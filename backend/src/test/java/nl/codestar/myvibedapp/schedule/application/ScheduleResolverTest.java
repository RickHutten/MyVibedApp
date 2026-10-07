package nl.codestar.myvibedapp.schedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.domain.MonthlyOccurrence;
import nl.codestar.myvibedapp.schedule.domain.MonthlyPattern;
import nl.codestar.myvibedapp.schedule.domain.RecurrenceLevel;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.ScheduleDay;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;
import nl.codestar.myvibedapp.schedule.domain.WorkingHours;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class ScheduleResolverTest {

    @Test
    void appliesMonthlyThenWeeklyThenDailyPrecedenceBeforeDefault() {
        final UUID officeId = UUID.randomUUID();
        final LocalDate date = LocalDate.of(2026, 3, 23);
        final WorkSchedule defaultSchedule = scheduleWith(ScheduleStatus.WORK_FROM_HOME, null);
        final RecurringRule daily = new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.DAYS, 1, Set.of(), null,
                LocalDate.of(2026, 1, 1), null, ScheduleStatus.OFFICE, officeId);
        final RecurringRule weekly = new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.WEEKS, 1, Set.of(DayOfWeek.MONDAY),
                null, LocalDate.of(2026, 1, 1), null,
                ScheduleStatus.WORK_FROM_HOME, null);
        final RecurringRule monthly = new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.MONTHS, 1, Set.of(),
                MonthlyPattern.weekdayOccurrence(DayOfWeek.MONDAY, MonthlyOccurrence.FOURTH),
                LocalDate.of(2026, 1, 1), null, ScheduleStatus.NON_WORKING, null);

        final ScheduleDay result = ScheduleResolver.resolve(defaultSchedule, List.of(daily, weekly, monthly), date);

        assertThat(result).isEqualTo(new ScheduleDay(ScheduleStatus.NON_WORKING, null));
    }

    private static WorkSchedule scheduleWith(final ScheduleStatus status, @Nullable final UUID officeId) {
        final Map<DayOfWeek, ScheduleDay> days = new EnumMap<>(DayOfWeek.class);
        for (final DayOfWeek day : DayOfWeek.values()) {
            days.put(day, new ScheduleDay(status, officeId));
        }
        return new WorkSchedule(new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0)), days);
    }
}
