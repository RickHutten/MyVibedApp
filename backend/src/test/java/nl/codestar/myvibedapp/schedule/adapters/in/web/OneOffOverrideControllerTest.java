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

class OneOffOverrideControllerTest extends PostgresIntegrationTest {

    private static final String ENDPOINT = "/api/work-schedule/overrides";
    private static final String JSON = "application/json";
    private static final String FIRST_OVERRIDE_START_DATE = "2026-10-08";
    private static final String FIRST_OVERRIDE_END_DATE = "2026-10-10";
    private static final String WORK_FROM_HOME_STATUS = "WORK_FROM_HOME";
    private static final String NON_WORKING_STATUS = "NON_WORKING";


    @Test
    void createsListsUpdatesAndDeletesOverride() throws Exception {
        final MvcResult created = mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(override(FIRST_OVERRIDE_START_DATE, FIRST_OVERRIDE_END_DATE, WORK_FROM_HOME_STATUS)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDate").value(FIRST_OVERRIDE_START_DATE))
                .andExpect(jsonPath("$.endDate").value(FIRST_OVERRIDE_END_DATE))
                .andExpect(jsonPath("$.status").value(WORK_FROM_HOME_STATUS))
                .andReturn();
        final UUID id = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), "$.id"));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put(ENDPOINT + "/" + id)
                        .contentType(JSON)
                        .content(override("2026-10-11", null, NON_WORKING_STATUS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2026-10-11"))
                .andExpect(jsonPath("$.endDate").doesNotExist())
                .andExpect(jsonPath("$.status").value(NON_WORKING_STATUS));

        mockMvc.perform(delete(ENDPOINT + "/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsOverlappingRangeAndLeavesExistingOverrideUnchanged() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(override(FIRST_OVERRIDE_START_DATE, FIRST_OVERRIDE_END_DATE, WORK_FROM_HOME_STATUS)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(override("2026-10-10", "2026-10-12", NON_WORKING_STATUS)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.conflictOverrideId").isNotEmpty());

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value(WORK_FROM_HOME_STATUS));
    }

    @Test
    void rejectsEndDateBeforeStartDate() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(JSON)
                        .content(override("2026-10-10", "2026-10-09", NON_WORKING_STATUS)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Override end date cannot be before its start date"));
    }

    private static String override(final String startDate, final @Nullable String endDate, final String status) {
        final String endDateJson = endDate == null ? "null" : "\"%s\"".formatted(endDate);
        return """
                {
                  "startDate":"%s",
                  "endDate":%s,
                  "status":"%s",
                  "officeId":null
                }
                """.formatted(startDate, endDateJson, status);
    }
}
