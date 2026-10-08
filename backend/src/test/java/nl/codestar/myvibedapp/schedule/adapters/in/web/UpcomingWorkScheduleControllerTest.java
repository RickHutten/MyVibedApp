package nl.codestar.myvibedapp.schedule.adapters.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.application.ScheduleStore;
import nl.codestar.myvibedapp.schedule.domain.OneOffOverride;
import nl.codestar.myvibedapp.schedule.domain.RecurrenceLevel;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.support.postgresql.FixedScheduleClockConfiguration;
import nl.codestar.myvibedapp.support.postgresql.PostgresIntegrationTest;
import nl.codestar.myvibedapp.support.scenario.ScheduleScenario;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(FixedScheduleClockConfiguration.class)
class UpcomingWorkScheduleControllerTest extends PostgresIntegrationTest {

    private static final String ENDPOINT = "/api/work-schedule/upcoming";
    private static final UUID DELETED_OFFICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private final ScheduleStore scheduleStore;

    @Autowired
    UpcomingWorkScheduleControllerTest(final ScheduleStore scheduleStore) {
        super();
        this.scheduleStore = scheduleStore;
    }

    @Test
    void returnsTodayAndNextDayWhilePreservingDeletedOfficeLabel() throws Exception {
        ScheduleScenario.builder()
                .deletedOffice(DELETED_OFFICE_ID, "Amsterdam office")
                .weeklyDay(DayOfWeek.WEDNESDAY, ScheduleStatus.OFFICE, DELETED_OFFICE_ID)
                .weeklyDay(DayOfWeek.THURSDAY, ScheduleStatus.WORK_FROM_HOME, null)
                .build()
                .persist(scheduleStore);

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.today.date").value("2026-10-07"))
                .andExpect(jsonPath("$.today.status").value("OFFICE"))
                .andExpect(jsonPath("$.today.officeLabel").value("Amsterdam office"))
                .andExpect(jsonPath("$.nextWorkingDay.date").value("2026-10-08"))
                .andExpect(jsonPath("$.nextWorkingDay.status").value("WORK_FROM_HOME"))
                .andExpect(jsonPath("$.nextWorkingDay.officeLabel").doesNotExist());
    }

    @Test
    void resolvesOneOffOverrideBeforeRecurringAndDefaultSchedule() throws Exception {
        final UUID officeId = UUID.fromString("00000000-0000-0000-0000-000000000005");
        ScheduleScenario.builder()
                .office(officeId, "Amsterdam office")
                .weeklyDay(DayOfWeek.THURSDAY, ScheduleStatus.OFFICE, officeId)
                .recurringRule(new RecurringRule(
                        UUID.randomUUID(), RecurrenceLevel.DAYS, 1, Set.of(), null,
                        LocalDate.of(2026, 10, 1), null, ScheduleStatus.NON_WORKING, null))
                .oneOffOverride(new OneOffOverride(
                        UUID.randomUUID(), LocalDate.of(2026, 10, 8), null,
                        ScheduleStatus.WORK_FROM_HOME, null))
                .build()
                .persist(scheduleStore);

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextWorkingDay.date").value("2026-10-08"))
                .andExpect(jsonPath("$.nextWorkingDay.status").value("WORK_FROM_HOME"));
    }

    @Test
    void returnsNullNextWorkingDayWhenNoFutureWorkIsConfigured() throws Exception {
        ScheduleScenario.builder().build().persist(scheduleStore);

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.today.status").value("NON_WORKING"))
                .andExpect(jsonPath("$.nextWorkingDay").value(Matchers.nullValue()));
    }
}
