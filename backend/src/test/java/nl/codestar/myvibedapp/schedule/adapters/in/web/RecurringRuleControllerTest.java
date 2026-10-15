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
    private static final String WORK_FROM_HOME = "WORK_FROM_HOME";
    private static final String JANUARY_FIRST = "2026-01-01";
    private static final String RULE_ID_PATH = "$.id";
    private static final String RULE_COUNT_PATH = "$.length()";
    private static final String DETAIL_PATH = "$.detail";

    @Test
    void createsUpdatesListsAndDeletesRule() throws Exception {
        final MvcResult created = mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule(WORK_FROM_HOME, JANUARY_FIRST, "2026-01-31", 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value("DAYS"))
                .andExpect(jsonPath("$.interval").value(2))
                .andExpect(jsonPath("$.endDate").value("2026-01-31"))
                .andReturn();
        final UUID id = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), RULE_ID_PATH));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(RULE_COUNT_PATH).value(1));

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

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(RULE_COUNT_PATH).value(1))
                .andExpect(jsonPath("$[0].level").value("MONTHS"))
                .andExpect(jsonPath("$[0].monthlyPattern.calendarDay").value(31));

        mockMvc.perform(delete(ENDPOINT + "/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(RULE_COUNT_PATH).value(0));
    }

    @Test
    void rejectsSameLevelOccurrenceConflictAndIdentifiesExistingRule() throws Exception {
        final MvcResult created = mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule(WORK_FROM_HOME, JANUARY_FIRST, null, 1)))
                .andExpect(status().isCreated())
                .andReturn();
        final UUID existingId = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), RULE_ID_PATH));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule("NON_WORKING", JANUARY_FIRST, null, 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.conflictRuleId").value(existingId.toString()));
    }

    @Test
    void rejectsNonPositiveInterval() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule(WORK_FROM_HOME, JANUARY_FIRST, null, 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEndDateBeforeStartDate() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule(WORK_FROM_HOME, "2026-01-02", JANUARY_FIRST, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(DETAIL_PATH).value("Rule end date cannot be before its start date"));
    }

    @Test
    void rejectsEditingUnknownRule() throws Exception {
        mockMvc.perform(put(ENDPOINT + "/" + UUID.randomUUID())
                        .contentType(JSON)
                        .content(dailyRule(WORK_FROM_HOME, JANUARY_FIRST, null, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(DETAIL_PATH).value("Recurring rule was not found"));
    }

    @Test
    void rejectsDeletingUnknownRule() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/" + UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(DETAIL_PATH).value("Recurring rule was not found"));
    }

    @Test
    void rejectsEditConflictAndLeavesPersistedRulesUnchanged() throws Exception {
        final MvcResult first = mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule(WORK_FROM_HOME, JANUARY_FIRST, "2026-01-03", 1)))
                .andExpect(status().isCreated())
                .andReturn();
        final UUID firstId = UUID.fromString(JsonPath.read(first.getResponse().getContentAsString(), RULE_ID_PATH));

        final MvcResult second = mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule("NON_WORKING", "2026-01-10", null, 1)))
                .andExpect(status().isCreated())
                .andReturn();
        final UUID secondId = UUID.fromString(JsonPath.read(second.getResponse().getContentAsString(), RULE_ID_PATH));

        mockMvc.perform(put(ENDPOINT + "/" + firstId)
                        .contentType(JSON)
                        .content(dailyRule(WORK_FROM_HOME, "2026-01-10", null, 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.conflictRuleId").value(secondId.toString()));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(RULE_COUNT_PATH).value(2))
                .andExpect(jsonPath("$[0].id").value(firstId.toString()))
                .andExpect(jsonPath("$[0].startDate").value("2026-01-01"))
                .andExpect(jsonPath("$[1].id").value(secondId.toString()))
                .andExpect(jsonPath("$[1].status").value("NON_WORKING"));
    }

    @Test
    void rejectsRuleUsingUnknownOffice() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule("OFFICE", JANUARY_FIRST, null, 1)
                                .replace("\"officeId\":null", "\"officeId\":\"%s\"".formatted(UUID.randomUUID()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(DETAIL_PATH).value("Office was not found"));
    }

    @Test
    void rejectsNewRuleUsingDeletedOffice() throws Exception {
        final MvcResult created = mockMvc.perform(post("/api/offices")
                        .contentType(JSON)
                        .content("""
                                {"label":"Amsterdam office","address":"Damrak 1, Amsterdam","latitude":52.37,"longitude":4.90}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        final UUID officeId = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), RULE_ID_PATH));

        mockMvc.perform(delete("/api/offices/" + officeId))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(dailyRule("OFFICE", JANUARY_FIRST, null, 1)
                                .replace("\"officeId\":null", "\"officeId\":\"%s\"".formatted(officeId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(DETAIL_PATH).value("A deleted office cannot be selected for a new rule"));
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
