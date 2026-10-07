package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import org.jspecify.annotations.Nullable;

record RecurringRuleResponse(
        UUID id,
        String level,
        int interval,
        Set<DayOfWeek> weekdays,
        @Nullable MonthlyPatternResponse monthlyPattern,
        LocalDate startDate,
        @Nullable LocalDate endDate,
        String status,
        @Nullable UUID officeId) {

    static RecurringRuleResponse from(final RecurringRule rule) {
        final MonthlyPatternResponse pattern = Optional.ofNullable(rule.monthlyPattern())
                .map(MonthlyPatternResponse::from)
                .orElse(null);
        return new RecurringRuleResponse(
                rule.id(), rule.level().name(), rule.interval(), rule.weekdays(), pattern,
                rule.startDate(), rule.endDate(), rule.status().name(), rule.officeId());
    }
}
