package nl.codestar.myvibedapp.schedule.adapters.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import nl.codestar.myvibedapp.support.postgresql.PostgresIntegrationTest;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class RecurringRuleControllerTest extends PostgresIntegrationTest {

    private static final String ENDPOINT = "/api/work-schedule/recurring-rules";
    private static final String JSON = "application/json";


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
            @Nullable final String endDate,
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
