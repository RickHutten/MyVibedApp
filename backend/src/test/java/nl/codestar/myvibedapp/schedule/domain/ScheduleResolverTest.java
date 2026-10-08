package nl.codestar.myvibedapp.schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class ScheduleResolverTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 7);
    private static final UUID OFFICE_ID = UUID.randomUUID();

    @Test
    void oneOffOverrideWinsOverRecurringRulesAndDefaultSchedule() {
        final WorkSchedule schedule = schedule(Map.of(
                DayOfWeek.THURSDAY, new ScheduleDay(ScheduleStatus.WORK_FROM_HOME, null)));
        final RecurringRule dailyRule = dailyRule(ScheduleStatus.OFFICE, OFFICE_ID, TODAY.minusDays(10));
        final OneOffOverride override = override(
                TODAY.plusDays(1), null, ScheduleStatus.NON_WORKING, null);

        final ResolvedScheduleDay result = ScheduleResolver.resolve(
                TODAY.plusDays(1), schedule, List.of(dailyRule), List.of(override));

        assertThat(result.status()).isEqualTo(ScheduleStatus.NON_WORKING);
        assertThat(result.officeId()).isNull();
    }

    @Test
    void monthlyRulesWinOverWeeklyAndDailyRules() {
        final LocalDate date = LocalDate.of(2026, 10, 12);
        final WorkSchedule schedule = schedule(Map.of(
                DayOfWeek.MONDAY, new ScheduleDay(ScheduleStatus.NON_WORKING, null)));
        final RecurringRule dailyRule = dailyRule(ScheduleStatus.NON_WORKING, null, date.minusDays(10));
        final RecurringRule weeklyRule = new RecurringRule(
                UUID.randomUUID(),
                RecurrenceLevel.WEEKS,
                1,
                Set.of(DayOfWeek.MONDAY),
                null,
                date.minusDays(7),
                null,
                ScheduleStatus.WORK_FROM_HOME,
                null);
        final RecurringRule monthlyRule = new RecurringRule(
                UUID.randomUUID(),
                RecurrenceLevel.MONTHS,
                1,
                Set.of(),
                MonthlyPattern.calendarDay(12),
                date.withDayOfMonth(1),
                null,
                ScheduleStatus.OFFICE,
                OFFICE_ID);

        final ResolvedScheduleDay result = ScheduleResolver.resolve(
                date, schedule, List.of(dailyRule, weeklyRule, monthlyRule), List.of());

        assertThat(result.status()).isEqualTo(ScheduleStatus.OFFICE);
        assertThat(result.officeId()).isEqualTo(OFFICE_ID);
    }

    @Test
    void fallsBackToTheDefaultWeeklyScheduleWhenNoRuleApplies() {
        final LocalDate date = LocalDate.of(2026, 10, 8);
        final WorkSchedule schedule = schedule(Map.of(
                DayOfWeek.THURSDAY, new ScheduleDay(ScheduleStatus.WORK_FROM_HOME, null)));

        final ResolvedScheduleDay result = ScheduleResolver.resolve(date, schedule, List.of(), List.of());

        assertThat(result.status()).isEqualTo(ScheduleStatus.WORK_FROM_HOME);
    }

    @Test
    void nextWorkingDayStartsTomorrowAndSkipsNonWorkingDates() {
        final WorkSchedule schedule = schedule(Map.of(
                DayOfWeek.MONDAY, new ScheduleDay(ScheduleStatus.WORK_FROM_HOME, null)));

        final var result = ScheduleResolver.findNextWorkingDay(TODAY.plusDays(1), schedule, List.of(), List.of());

        assertThat(result).hasValueSatisfying(day -> {
            assertThat(day.date()).isEqualTo(LocalDate.of(2026, 10, 12));
            assertThat(day.status()).isEqualTo(ScheduleStatus.WORK_FROM_HOME);
        });
    }

    @Test
    void returnsNoNextWorkingDayWhenAllConfiguredOutcomesAreNonWorking() {
        final WorkSchedule schedule = schedule(Map.of());

        final var result = ScheduleResolver.findNextWorkingDay(TODAY, schedule, List.of(), List.of());

        assertThat(result).isEmpty();
    }

    private static WorkSchedule schedule(final Map<DayOfWeek, ScheduleDay> overrides) {
        final Map<DayOfWeek, ScheduleDay> days = new EnumMap<>(DayOfWeek.class);
        for (final DayOfWeek day : DayOfWeek.values()) {
            days.put(day, new ScheduleDay(ScheduleStatus.NON_WORKING, null));
        }
        days.putAll(overrides);
        return new WorkSchedule(new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0)), days);
    }

    private static RecurringRule dailyRule(
            final ScheduleStatus status, final @Nullable UUID officeId, final LocalDate startDate) {
        return new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.DAYS, 1, Set.of(), null,
                startDate, null, status, officeId);
    }

    private static OneOffOverride override(
            final LocalDate startDate, final @Nullable LocalDate endDate,
            final ScheduleStatus status, final @Nullable UUID officeId) {
        return new OneOffOverride(UUID.randomUUID(), startDate, endDate, status, officeId);
    }
}
