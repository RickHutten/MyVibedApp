package nl.codestar.myvibedapp.schedule.adapters.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("local")
@Testcontainers
class WorkScheduleControllerTest {

    private static final String WORK_SCHEDULE_ENDPOINT = "/api/work-schedule";
    private static final String JSON_CONTENT_TYPE = "application/json";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.6");

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configurePostgres(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    WorkScheduleControllerTest(
            @Autowired final WebApplicationContext context,
            @Autowired final JdbcTemplate jdbcTemplate) {
        mockMvc = webAppContextSetup(context).build();
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.execute("truncate table one_off_schedule_overrides, recurring_schedule_rules, work_schedule_days, saved_offices");
        jdbcTemplate.execute("truncate table work_schedule");
        jdbcTemplate.execute("insert into work_schedule (id, start_time, end_time) values (true, '09:00', '17:00')");
        jdbcTemplate.execute("insert into work_schedule_days (day_of_week, status, office_id) "
                + "select day_number, 'NON_WORKING', null from generate_series(1, 7) as day_number");
    }

    @Test
    void returnsDefaultScheduleWithAllDaysNonWorking() throws Exception {
        mockMvc.perform(get(WORK_SCHEDULE_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workingHours.start").value("09:00"))
                .andExpect(jsonPath("$.workingHours.end").value("17:00"))
                .andExpect(jsonPath("$.days").isArray())
                .andExpect(jsonPath("$.days.length()").value(7))
                .andExpect(jsonPath("$.days[0].status").value("NON_WORKING"))
                .andExpect(jsonPath("$.days[6].day").value("SUNDAY"));
    }

    @Test
    void savesWeeklyStatusesAndSharedHours() throws Exception {
        mockMvc.perform(put(WORK_SCHEDULE_ENDPOINT)
                        .contentType(JSON_CONTENT_TYPE)
                        .content(scheduleJson("08:30", "16:30", "WORK_FROM_HOME")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workingHours.start").value("08:30"))
                .andExpect(jsonPath("$.days[0].status").value("WORK_FROM_HOME"))
                .andExpect(jsonPath("$.days[5].status").value("NON_WORKING"));

        mockMvc.perform(get(WORK_SCHEDULE_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workingHours.start").value("08:30"))
                .andExpect(jsonPath("$.days[0].status").value("WORK_FROM_HOME"));

        Assertions.assertThat(jdbcTemplate.queryForObject(
                        "select start_time::text from work_schedule where id = true", String.class))
                .isEqualTo("08:30:00");
    }

    @Test
    void rejectsOfficeDayWithoutOffice() throws Exception {
        mockMvc.perform(put(WORK_SCHEDULE_ENDPOINT)
                        .contentType(JSON_CONTENT_TYPE)
                        .content(scheduleJson("09:00", "17:00", "OFFICE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("An office day requires an office"));
    }

    @Test
    void softDeletesOfficeWithoutBreakingExistingAssignment() throws Exception {
        final MvcResult created = mockMvc.perform(post("/api/offices")
                        .contentType(JSON_CONTENT_TYPE)
                        .content("""
                                {"label":"Amsterdam office","address":"Damrak 1, Amsterdam","latitude":52.37,"longitude":4.90}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        final UUID officeId = UUID.fromString(JsonPath.read(
                created.getResponse().getContentAsString(), "$.id"));

        mockMvc.perform(put(WORK_SCHEDULE_ENDPOINT)
                        .contentType(JSON_CONTENT_TYPE)
                        .content(scheduleJsonWithOffice(officeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days[0].officeId").value(officeId.toString()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/offices/{id}", officeId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(WORK_SCHEDULE_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days[0].officeId").value(officeId.toString()))
                .andExpect(jsonPath("$.offices[0].deleted").value(true));
    }

    @Test
    void rejectsAddressSearchQueryLongerThan200Characters() throws Exception {
        mockMvc.perform(get("/api/offices/search").param("query", "a".repeat(201)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNoSuggestionsForBlankAddressSearchQuery() throws Exception {
        mockMvc.perform(get("/api/offices/search").param("query", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private static String scheduleJson(final String start, final String end, final String mondayStatus) {
        return """
                {
                  "workingHours": {"start":"%s","end":"%s"},
                  "days": [
                    {"day":"MONDAY","status":"%s"},
                    {"day":"TUESDAY","status":"NON_WORKING"},
                    {"day":"WEDNESDAY","status":"NON_WORKING"},
                    {"day":"THURSDAY","status":"NON_WORKING"},
                    {"day":"FRIDAY","status":"NON_WORKING"},
                    {"day":"SATURDAY","status":"NON_WORKING"},
                    {"day":"SUNDAY","status":"NON_WORKING"}
                  ]
                }
                """.formatted(start, end, mondayStatus);
    }

    private static String scheduleJsonWithOffice(final UUID officeId) {
        return scheduleJson("09:00", "17:00", "OFFICE").replace(
                "{\"day\":\"MONDAY\",\"status\":\"OFFICE\"}",
                "{\"day\":\"MONDAY\",\"status\":\"OFFICE\",\"officeId\":\"%s\"}".formatted(officeId));
    }
}
