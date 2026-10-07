package nl.codestar.myvibedapp.schedule.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class RecurringRuleEvaluator {

    private RecurringRuleEvaluator() {
    }

    public static boolean matches(final RecurringRule rule, final LocalDate date) {
        if (date.isBefore(rule.startDate()) || (rule.endDate() != null && date.isAfter(rule.endDate()))) {
            return false;
        }
        return switch (rule.level()) {
            case DAYS -> daysMatch(rule, date);
            case WEEKS -> weeksMatch(rule, date);
            case MONTHS -> monthsMatch(rule, date);
        };
    }

    private static boolean daysMatch(final RecurringRule rule, final LocalDate date) {
        return ChronoUnit.DAYS.between(rule.startDate(), date) % rule.interval() == 0;
    }

    private static boolean weeksMatch(final RecurringRule rule, final LocalDate date) {
        final long weeks = ChronoUnit.WEEKS.between(
                rule.startDate().with(DayOfWeek.MONDAY), date.with(DayOfWeek.MONDAY));
        return weeks % rule.interval() == 0 && rule.weekdays().contains(date.getDayOfWeek());
    }

    private static boolean monthsMatch(final RecurringRule rule, final LocalDate date) {
        final long months = ChronoUnit.MONTHS.between(
                YearMonth.from(rule.startDate()), YearMonth.from(date));
        final @Nullable MonthlyPattern pattern = rule.monthlyPattern();
        if (months % rule.interval() != 0 || pattern == null) {
            return false;
        }
        if (pattern.type() == MonthlyPatternType.CALENDAR_DAY) {
            return date.getDayOfMonth() == Objects.requireNonNull(pattern.calendarDay());
        }
        final MonthlyOccurrence occurrence = Objects.requireNonNull(pattern.occurrence());
        final DayOfWeek weekday = Objects.requireNonNull(pattern.weekday());
        final int ordinal = switch (occurrence) {
            case FIRST -> 1;
            case SECOND -> 2;
            case THIRD -> 3;
            case FOURTH -> 4;
            case LAST -> -1;
        };
        final LocalDate expected;
        if (ordinal > 0) {
            expected = date.with(TemporalAdjusters.dayOfWeekInMonth(ordinal, weekday));
        } else {
            expected = date.with(TemporalAdjusters.lastInMonth(weekday));
        }
        return date.equals(expected);
    }
}
