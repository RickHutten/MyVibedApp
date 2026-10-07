package nl.codestar.myvibedapp.schedule.adapters.out.persistence;

import static nl.codestar.myvibedapp.jooq.Tables.RECURRING_SCHEDULE_RULES;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import nl.codestar.myvibedapp.schedule.domain.MonthlyOccurrence;
import nl.codestar.myvibedapp.schedule.domain.MonthlyPattern;
import nl.codestar.myvibedapp.schedule.domain.RecurrenceLevel;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import org.jooq.Record;
import org.jspecify.annotations.Nullable;

final class JooqRecurringRuleMapper {

    private static final String CALENDAR_DAY = "CALENDAR_DAY";

    private JooqRecurringRuleMapper() {
    }

    static RecurringRule from(final Record row) {
        final RecurrenceLevel level = RecurrenceLevel.valueOf(row.get(RECURRING_SCHEDULE_RULES.RECURRENCE_LEVEL));
        final MonthlyPattern monthlyPattern = monthlyPattern(row);
        return new RecurringRule(
                row.get(RECURRING_SCHEDULE_RULES.ID),
                level,
                row.get(RECURRING_SCHEDULE_RULES.INTERVAL_VALUE),
                weekdays(row.get(RECURRING_SCHEDULE_RULES.WEEKDAY_MASK)),
                monthlyPattern,
                row.get(RECURRING_SCHEDULE_RULES.START_DATE),
                row.get(RECURRING_SCHEDULE_RULES.END_DATE),
                ScheduleStatus.valueOf(row.get(RECURRING_SCHEDULE_RULES.STATUS)),
                row.get(RECURRING_SCHEDULE_RULES.OFFICE_ID));
    }

    static int weekdayMask(final RecurringRule rule) {
        return rule.weekdays().stream()
                .mapToInt(day -> 1 << (day.getValue() - 1))
                .reduce(0, (left, right) -> left | right);
    }

    static @Nullable Short calendarDay(final RecurringRule rule) {
        return Optional.ofNullable(rule.monthlyPattern())
                .map(MonthlyPattern::calendarDay)
                .map(Integer::shortValue)
                .orElse(null);
    }

    static @Nullable Short monthlyWeekday(final RecurringRule rule) {
        return Optional.ofNullable(rule.monthlyPattern())
                .map(MonthlyPattern::weekday)
                .map(DayOfWeek::getValue)
                .map(Integer::shortValue)
                .orElse(null);
    }

    static @Nullable String monthlyOccurrence(final RecurringRule rule) {
        return Optional.ofNullable(rule.monthlyPattern())
                .map(MonthlyPattern::occurrence)
                .map(Enum::name)
                .orElse(null);
    }

    static @Nullable String monthlyPatternType(final RecurringRule rule) {
        return Optional.ofNullable(rule.monthlyPattern())
                .map(MonthlyPattern::type)
                .map(Enum::name)
                .orElse(null);
    }

    private static @Nullable MonthlyPattern monthlyPattern(final Record row) {
        final String patternType = row.get(RECURRING_SCHEDULE_RULES.MONTHLY_PATTERN_TYPE);
        if (patternType == null) {
            return null;
        }
        if (CALENDAR_DAY.equals(patternType)) {
            return MonthlyPattern.calendarDay(row.get(RECURRING_SCHEDULE_RULES.MONTHLY_CALENDAR_DAY));
        }
        return MonthlyPattern.weekdayOccurrence(
                DayOfWeek.of(row.get(RECURRING_SCHEDULE_RULES.MONTHLY_WEEKDAY)),
                MonthlyOccurrence.valueOf(row.get(RECURRING_SCHEDULE_RULES.MONTHLY_OCCURRENCE)));
    }

    private static Set<DayOfWeek> weekdays(final int mask) {
        final Set<DayOfWeek> weekdays = EnumSet.noneOf(DayOfWeek.class);
        for (final DayOfWeek day : DayOfWeek.values()) {
            if ((mask & (1 << (day.getValue() - 1))) != 0) {
                weekdays.add(day);
            }
        }
        return weekdays;
    }
}
