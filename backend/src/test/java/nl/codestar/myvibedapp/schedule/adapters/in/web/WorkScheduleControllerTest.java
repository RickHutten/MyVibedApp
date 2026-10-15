package nl.codestar.myvibedapp.schedule.adapters.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import nl.codestar.myvibedapp.support.postgresql.PostgresIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class WorkScheduleControllerTest extends PostgresIntegrationTest {

    private static final String WORK_SCHEDULE_ENDPOINT = "/api/work-schedule";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_START_TIME = "09:00";
    private static final String DEFAULT_END_TIME = "17:00";
    private static final String WORK_FROM_HOME_STATUS = "WORK_FROM_HOME";
    private static final String MONDAY_STATUS_PATH = "$.days[0].status";
    private static final String MONDAY_OFFICE_ID_PATH = "$.days[0].officeId";


    @Test
    void returnsDefaultScheduleWithAllDaysNonWorking() throws Exception {
        mockMvc.perform(get(WORK_SCHEDULE_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workingHours.start").value(DEFAULT_START_TIME))
                .andExpect(jsonPath("$.workingHours.end").value(DEFAULT_END_TIME))
                .andExpect(jsonPath("$.days").isArray())
                .andExpect(jsonPath("$.days.length()").value(7))
                .andExpect(jsonPath("$.days[0].status").value("NON_WORKING"))
                .andExpect(jsonPath("$.days[6].day").value("SUNDAY"));
    }

    @Test
    void savesWeeklyStatusesAndSharedHours() throws Exception {
        mockMvc.perform(put(WORK_SCHEDULE_ENDPOINT)
                        .contentType(JSON_CONTENT_TYPE)
                        .content(scheduleJson("08:30", "16:30", WORK_FROM_HOME_STATUS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workingHours.start").value("08:30"))
                .andExpect(jsonPath(MONDAY_STATUS_PATH).value(WORK_FROM_HOME_STATUS))
                .andExpect(jsonPath("$.days[5].status").value("NON_WORKING"));

        mockMvc.perform(get(WORK_SCHEDULE_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workingHours.start").value("08:30"))
                .andExpect(jsonPath(MONDAY_STATUS_PATH).value(WORK_FROM_HOME_STATUS));

        Assertions.assertThat(jdbcTemplate.queryForObject(
                        "select start_time::text from work_schedule where id = true", String.class))
                .isEqualTo("08:30:00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"WORK_FROM_HOME", "NON_WORKING"})
    void replacesOfficeDayWithNonOfficeDay(final String replacementStatus) throws Exception {
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
                .andExpect(status().isOk());

        mockMvc.perform(put(WORK_SCHEDULE_ENDPOINT)
                        .contentType(JSON_CONTENT_TYPE)
                        .content(scheduleJson(DEFAULT_START_TIME, DEFAULT_END_TIME, replacementStatus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MONDAY_STATUS_PATH).value(replacementStatus))
                .andExpect(jsonPath(MONDAY_OFFICE_ID_PATH).doesNotExist());

        mockMvc.perform(get(WORK_SCHEDULE_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MONDAY_STATUS_PATH).value(replacementStatus))
                .andExpect(jsonPath(MONDAY_OFFICE_ID_PATH).doesNotExist());

        Assertions.assertThat(jdbcTemplate.queryForObject(
                        "select office_id is null from work_schedule_days where day_of_week = 1", Boolean.class))
                .isTrue();
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
        return scheduleJson(DEFAULT_START_TIME, DEFAULT_END_TIME, "OFFICE").replace(
                "{\"day\":\"MONDAY\",\"status\":\"OFFICE\"}",
                "{\"day\":\"MONDAY\",\"status\":\"OFFICE\",\"officeId\":\"%s\"}".formatted(officeId));
    }
}
