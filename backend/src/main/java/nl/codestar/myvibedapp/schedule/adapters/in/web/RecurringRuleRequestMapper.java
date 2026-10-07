package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import nl.codestar.myvibedapp.schedule.domain.MonthlyOccurrence;
import nl.codestar.myvibedapp.schedule.domain.MonthlyPattern;
import nl.codestar.myvibedapp.schedule.domain.RecurrenceLevel;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import org.jspecify.annotations.Nullable;

final class RecurringRuleRequestMapper {

    private RecurringRuleRequestMapper() {
    }

    static RecurringRule toDomain(final UUID id, final RecurringRuleRequest request) {
        final RecurrenceLevel level = RecurrenceLevel.valueOf(request.level());
        final ScheduleStatus status = ScheduleStatus.valueOf(request.status());
        final Set<DayOfWeek> weekdays = request.weekdays().stream()
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toUnmodifiableSet());
        final MonthlyPattern pattern = monthlyPattern(request.monthlyPattern());
        return new RecurringRule(
                id, level, request.interval(), weekdays, pattern, request.startDate(), request.endDate(), status,
                request.officeId());
    }

    private static @Nullable MonthlyPattern monthlyPattern(@Nullable final MonthlyPatternRequest request) {
        if (request == null) {
            return null;
        }
        return switch (request.type()) {
            case "CALENDAR_DAY" -> MonthlyPattern.calendarDay(requiredCalendarDay(request));
            case "WEEKDAY_OCCURRENCE" -> MonthlyPattern.weekdayOccurrence(
                    requiredWeekday(request), MonthlyOccurrence.valueOf(requiredOccurrence(request)));
            default -> throw new IllegalArgumentException("Unknown monthly pattern");
        };
    }

    private static int requiredCalendarDay(final MonthlyPatternRequest request) {
        final Integer calendarDay = request.calendarDay();
        if (calendarDay == null) {
            throw new IllegalArgumentException("A calendar-day pattern requires a calendar day");
        }
        return calendarDay;
    }

    private static DayOfWeek requiredWeekday(final MonthlyPatternRequest request) {
        final DayOfWeek weekday = request.weekday();
        if (weekday == null) {
            throw new IllegalArgumentException("A weekday-occurrence pattern requires a weekday");
        }
        return weekday;
    }

    private static String requiredOccurrence(final MonthlyPatternRequest request) {
        final String occurrence = request.occurrence();
        if (occurrence == null) {
            throw new IllegalArgumentException("A weekday-occurrence pattern requires an occurrence");
        }
        return occurrence;
    }
}
