package nl.codestar.myvibedapp.schedule.domain;

import java.time.DayOfWeek;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record MonthlyPattern(
        MonthlyPatternType type,
        @Nullable Integer calendarDay,
        @Nullable DayOfWeek weekday,
        @Nullable MonthlyOccurrence occurrence) {

    public MonthlyPattern {
        Objects.requireNonNull(type, "type");
        if (type == MonthlyPatternType.CALENDAR_DAY) {
            if (calendarDay == null || calendarDay < 1 || calendarDay > 31) {
                throw new IllegalArgumentException("Monthly calendar day must be between 1 and 31");
            }
            if (weekday != null || occurrence != null) {
                throw new IllegalArgumentException("A calendar-day pattern cannot include a weekday occurrence");
            }
        } else {
            if (calendarDay != null || weekday == null || occurrence == null) {
                throw new IllegalArgumentException("A weekday-occurrence pattern requires a weekday and occurrence");
            }
        }
    }

    public static MonthlyPattern calendarDay(final int day) {
        return new MonthlyPattern(MonthlyPatternType.CALENDAR_DAY, day, null, null);
    }

    public static MonthlyPattern weekdayOccurrence(final DayOfWeek weekday, final MonthlyOccurrence occurrence) {
        return new MonthlyPattern(MonthlyPatternType.WEEKDAY_OCCURRENCE, null, weekday, occurrence);
    }
}
