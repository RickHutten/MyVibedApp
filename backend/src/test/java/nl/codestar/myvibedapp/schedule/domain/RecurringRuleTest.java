package nl.codestar.myvibedapp.schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class RecurringRuleTest {


    @Test
    void evaluatesEverySecondMondayFromAnchoredWeek() {
        final RecurringRule rule = weeklyRule(2, Set.of(DayOfWeek.MONDAY), LocalDate.of(2026, 1, 5), null);

        assertThat(RecurringRuleEvaluator.matches(rule, LocalDate.of(2026, 1, 5))).isTrue();
        assertThat(RecurringRuleEvaluator.matches(rule, LocalDate.of(2026, 1, 12))).isFalse();
        assertThat(RecurringRuleEvaluator.matches(rule, LocalDate.of(2026, 1, 19))).isTrue();
        assertThat(RecurringRuleEvaluator.matches(rule, LocalDate.of(2026, 1, 4))).isFalse();
    }

    @Test
    void skipsMonthsWithoutConfiguredCalendarDay() {
        final RecurringRule rule = new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.MONTHS, 1, Set.of(), MonthlyPattern.calendarDay(31),
                LocalDate.of(2026, 1, 1), null, ScheduleStatus.WORK_FROM_HOME, null);

        assertThat(RecurringRuleEvaluator.matches(rule, LocalDate.of(2026, 1, 31))).isTrue();
        assertThat(RecurringRuleEvaluator.matches(rule, LocalDate.of(2026, 2, 28))).isFalse();
        assertThat(RecurringRuleEvaluator.matches(rule, LocalDate.of(2026, 3, 31))).isTrue();
    }

    @Test
    void distinguishesFourthWeekdayFromLastWeekday() {
        final RecurringRule fourth = new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.MONTHS, 1, Set.of(), MonthlyPattern.weekdayOccurrence(
                        DayOfWeek.MONDAY, MonthlyOccurrence.FOURTH),
                LocalDate.of(2026, 1, 1), null, ScheduleStatus.NON_WORKING, null);
        final RecurringRule last = new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.MONTHS, 1, Set.of(), MonthlyPattern.weekdayOccurrence(
                        DayOfWeek.MONDAY, MonthlyOccurrence.LAST),
                LocalDate.of(2026, 1, 1), null, ScheduleStatus.NON_WORKING, null);

        assertThat(RecurringRuleEvaluator.matches(fourth, LocalDate.of(2026, 1, 26))).isTrue();
        assertThat(RecurringRuleEvaluator.matches(last, LocalDate.of(2026, 1, 26))).isTrue();
        assertThat(RecurringRuleEvaluator.matches(fourth, LocalDate.of(2026, 3, 23))).isTrue();
        assertThat(RecurringRuleEvaluator.matches(last, LocalDate.of(2026, 3, 23))).isFalse();
    }

    @Test
    void endDateIsInclusive() {
        final RecurringRule rule = dailyRule(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3));

        assertThat(RecurringRuleEvaluator.matches(rule, LocalDate.of(2026, 1, 3))).isTrue();
        assertThat(RecurringRuleEvaluator.matches(rule, LocalDate.of(2026, 1, 4))).isFalse();
    }

    @Test
    void rejectsSameLevelRulesWithAnActualOccurrenceConflict() {
        final RecurringRule first = dailyRule(2, LocalDate.of(2026, 1, 1), null);
        final RecurringRule second = dailyRule(3, LocalDate.of(2026, 1, 5), null);

        assertThat(RecurringRuleConflictChecker.findConflict(second, Set.of(first)))
                .get()
                .extracting(RecurringRule::id)
                .isEqualTo(first.id());
    }

    @Test
    void allowsSameLevelRulesWhoseActiveRangesDoNotOverlap() {
        final RecurringRule first = dailyRule(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));
        final RecurringRule second = dailyRule(1, LocalDate.of(2026, 1, 11), null);

        assertThat(RecurringRuleConflictChecker.findConflict(second, Set.of(first))).isEmpty();
    }

    @Test
    void requiresWeeklyDaysAndOfficeForOfficeOutcome() {
        final UUID id = UUID.randomUUID();
        final Set<DayOfWeek> weekdays = Set.of();
        final LocalDate start = LocalDate.of(2026, 1, 1);
        assertThatThrownBy(() -> new RecurringRule(
                id, RecurrenceLevel.WEEKS, 1, weekdays, null, start, null,
                ScheduleStatus.WORK_FROM_HOME, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weekday");
        final UUID officeRuleId = UUID.randomUUID();
        assertThatThrownBy(() -> new RecurringRule(
                officeRuleId, RecurrenceLevel.DAYS, 1, weekdays, null, start, null,
                ScheduleStatus.OFFICE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("office");
    }

    private static RecurringRule dailyRule(
            final int interval,
            final LocalDate start,
            @Nullable final LocalDate end) {
        return new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.DAYS, interval, Set.of(), null, start, end,
                ScheduleStatus.WORK_FROM_HOME, null);
    }

    private static RecurringRule weeklyRule(
            final int interval,
            final Set<DayOfWeek> weekdays,
            final LocalDate start,
            @Nullable final LocalDate end) {
        return new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.WEEKS, interval, weekdays, null, start, end,
                ScheduleStatus.WORK_FROM_HOME, null);
    }
}
