package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.time.DayOfWeek;
import java.util.Optional;
import nl.codestar.myvibedapp.schedule.domain.MonthlyPattern;
import org.jspecify.annotations.Nullable;

record MonthlyPatternResponse(
        String type,
        @Nullable Integer calendarDay,
        @Nullable DayOfWeek weekday,
        @Nullable String occurrence) {

    static MonthlyPatternResponse from(final MonthlyPattern pattern) {
        final String occurrence = Optional.ofNullable(pattern.occurrence())
                .map(Enum::name)
                .orElse(null);
        return new MonthlyPatternResponse(
                pattern.type().name(), pattern.calendarDay(), pattern.weekday(), occurrence);
    }
}
