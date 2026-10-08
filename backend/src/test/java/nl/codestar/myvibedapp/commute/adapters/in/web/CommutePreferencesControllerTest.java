package nl.codestar.myvibedapp.commute.adapters.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.codestar.myvibedapp.support.postgresql.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;

class CommutePreferencesControllerTest extends PostgresIntegrationTest {

    private static final String ENDPOINT = "/api/commute/preferences";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String WAKE_UP_LEAD_PATH = "$.wakeUpLeadMinutes";
    private static final String TRANSIT_ACCESS_BUFFER_PATH = "$.transitAccessBufferMinutes";
    private static final String OFFICE_ARRIVAL_LEAD_PATH = "$.officeArrivalLeadMinutes";

    @Test
    void returnsDefaultPreferences() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(WAKE_UP_LEAD_PATH).value(45))
                .andExpect(jsonPath(TRANSIT_ACCESS_BUFFER_PATH).value(5))
                .andExpect(jsonPath(OFFICE_ARRIVAL_LEAD_PATH).value(5));
    }

    @Test
    void returnsDefaultsWhenThePreferenceRowIsMissing() throws Exception {
        jdbcTemplate.update("truncate table commute_preferences");

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(WAKE_UP_LEAD_PATH).value(45))
                .andExpect(jsonPath(TRANSIT_ACCESS_BUFFER_PATH).value(5))
                .andExpect(jsonPath(OFFICE_ARRIVAL_LEAD_PATH).value(5));
    }

    @Test
    void replacesAndReloadsPreferences() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .contentType(JSON_CONTENT_TYPE)
                        .content("""
                                {
                                  "wakeUpLeadMinutes": 0,
                                  "transitAccessBufferMinutes": 17,
                                  "officeArrivalLeadMinutes": 12
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath(WAKE_UP_LEAD_PATH).value(0))
                .andExpect(jsonPath(TRANSIT_ACCESS_BUFFER_PATH).value(17))
                .andExpect(jsonPath(OFFICE_ARRIVAL_LEAD_PATH).value(12));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(WAKE_UP_LEAD_PATH).value(0))
                .andExpect(jsonPath(TRANSIT_ACCESS_BUFFER_PATH).value(17))
                .andExpect(jsonPath(OFFICE_ARRIVAL_LEAD_PATH).value(12));
    }

    @Test
    void acceptsLargeWholeMinuteValues() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .contentType(JSON_CONTENT_TYPE)
                        .content("""
                                {
                                  "wakeUpLeadMinutes": 2147483647,
                                  "transitAccessBufferMinutes": 2147483647,
                                  "officeArrivalLeadMinutes": 2147483647
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsNegativeMinutes() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .contentType(JSON_CONTENT_TYPE)
                        .content("""
                                {
                                  "wakeUpLeadMinutes": -1,
                                  "transitAccessBufferMinutes": 5,
                                  "officeArrivalLeadMinutes": 5
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsFractionalMinutes() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .contentType(JSON_CONTENT_TYPE)
                        .content("""
                                {
                                  "wakeUpLeadMinutes": 5.5,
                                  "transitAccessBufferMinutes": 5,
                                  "officeArrivalLeadMinutes": 5
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
