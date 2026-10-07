package nl.codestar.myvibedapp.schedule.adapters.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

record RecurringRuleRequest(
        @NotNull String level,
        @Positive int interval,
        @NotNull Set<String> weekdays,
        @Nullable @Valid MonthlyPatternRequest monthlyPattern,
        @NotNull LocalDate startDate,
        @Nullable LocalDate endDate,
        @NotNull String status,
        @Nullable UUID officeId) {
}
