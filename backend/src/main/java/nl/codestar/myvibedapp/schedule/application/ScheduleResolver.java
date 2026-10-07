package nl.codestar.myvibedapp.schedule.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.RecurringRuleEvaluator;
import nl.codestar.myvibedapp.schedule.domain.ScheduleDay;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;

@FunctionalInterface
public interface ScheduleResolver {

    @SuppressWarnings("unused")
    ScheduleDay resolve(LocalDate date);

    static ScheduleDay resolve(
            final WorkSchedule schedule,
            final List<RecurringRule> rules,
            final LocalDate date) {
        return rules.stream()
                .filter(rule -> RecurringRuleEvaluator.matches(rule, date))
                .max(Comparator.comparingInt(ScheduleResolver::levelRank))
                .map(rule -> new ScheduleDay(rule.status(), rule.officeId()))
                .orElseGet(() -> schedule.days().get(date.getDayOfWeek()));
    }

    private static int levelRank(final RecurringRule rule) {
        return switch (rule.level()) {
            case MONTHS -> 3;
            case WEEKS -> 2;
            case DAYS -> 1;
        };
    }
}
