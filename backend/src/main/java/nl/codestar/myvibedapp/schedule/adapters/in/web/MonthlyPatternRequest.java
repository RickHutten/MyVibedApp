package nl.codestar.myvibedapp.schedule.adapters.in.web;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import org.jspecify.annotations.Nullable;

record MonthlyPatternRequest(
        @NotNull String type,
        @Nullable Integer calendarDay,
        @Nullable DayOfWeek weekday,
        @Nullable String occurrence) {
}
