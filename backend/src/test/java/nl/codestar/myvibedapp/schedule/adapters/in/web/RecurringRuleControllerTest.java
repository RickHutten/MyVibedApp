package nl.codestar.myvibedapp.schedule.adapters.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
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
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("local")
@Testcontainers
class RecurringRuleControllerTest {

    private static final String ENDPOINT = "/api/work-schedule/recurring-rules";
    private static final String JSON = "application/json";

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

    RecurringRuleControllerTest(
            @Autowired final WebApplicationContext context,
            @Autowired final JdbcTemplate jdbcTemplate) {
        mockMvc = webAppContextSetup(context).build();
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.execute("truncate table recurring_schedule_rules, work_schedule_days, saved_offices");
        jdbcTemplate.execute("truncate table work_schedule");
        jdbcTemplate.execute("insert into work_schedule (id, start_time, end_time) values (true, '09:00', '17:00')");
        jdbcTemplate.execute("insert into work_schedule_days (day_of_week, status, office_id) "
                + "select day_number, 'NON_WORKING', null from generate_series(1, 7) as day_number");
    }

    @Test
    void createsUpdatesListsAndDeletesRule() throws Exception {
        final MvcResult created = mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule("WORK_FROM_HOME", "2026-01-01", "2026-01-31", 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value("DAYS"))
                .andExpect(jsonPath("$.interval").value(2))
                .andExpect(jsonPath("$.endDate").value("2026-01-31"))
                .andReturn();
        final UUID id = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), "$.id"));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put(ENDPOINT + "/" + id)
                        .contentType(JSON)
                        .content("""
                                {
                                  "level":"MONTHS",
                                  "interval":1,
                                  "weekdays":[],
                                  "monthlyPattern":{"type":"CALENDAR_DAY","calendarDay":31},
                                  "startDate":"2026-01-01",
                                  "endDate":null,
                                  "status":"NON_WORKING",
                                  "officeId":null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("MONTHS"))
                .andExpect(jsonPath("$.monthlyPattern.calendarDay").value(31));

        mockMvc.perform(delete(ENDPOINT + "/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsSameLevelOccurrenceConflictAndIdentifiesExistingRule() throws Exception {
        final MvcResult created = mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule("WORK_FROM_HOME", "2026-01-01", null, 1)))
                .andExpect(status().isCreated())
                .andReturn();
        final UUID existingId = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), "$.id"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule("NON_WORKING", "2026-01-01", null, 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.conflictRuleId").value(existingId.toString()));
    }

    private static String dailyRule(
            final String status,
            final String startDate,
            final String endDate,
            final int interval) {
        return """
                {
                  "level":"DAYS",
                  "interval":%d,
                  "weekdays":[],
                  "monthlyPattern":null,
                  "startDate":"%s",
                  "endDate":%s,
                  "status":"%s",
                  "officeId":null
                }
                """.formatted(interval, startDate, endDate == null ? "null" : "\"%s\"".formatted(endDate), status);
    }
}
