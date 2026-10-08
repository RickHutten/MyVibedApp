package nl.codestar.myvibedapp.schedule.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public final class ScheduleResolver {

    private static final long MAX_SEARCH_DAYS = 366L * 400L;
    private static final List<RecurrenceLevel> RECURRENCE_PRECEDENCE = List.of(
            RecurrenceLevel.MONTHS, RecurrenceLevel.WEEKS, RecurrenceLevel.DAYS);

    private ScheduleResolver() {
    }

    public static ResolvedScheduleDay resolve(
            final LocalDate date,
            final WorkSchedule defaultSchedule,
            final Collection<RecurringRule> recurringRules,
            final Collection<OneOffOverride> overrides) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(defaultSchedule, "defaultSchedule");
        Objects.requireNonNull(recurringRules, "recurringRules");
        Objects.requireNonNull(overrides, "overrides");

        final Optional<OneOffOverride> override = overrides.stream()
                .filter(candidate -> candidate.covers(date))
                .findFirst();
        if (override.isPresent()) {
            final OneOffOverride matchingOverride = override.orElseThrow();
            return resolved(date, matchingOverride.status(), matchingOverride.officeId());
        }

        final Optional<RecurringRule> recurringRule = RECURRENCE_PRECEDENCE.stream()
                .flatMap(level -> recurringRules.stream()
                        .filter(candidate -> candidate.level() == level)
                        .filter(candidate -> RecurringRuleEvaluator.matches(candidate, date)))
                .findFirst();
        if (recurringRule.isPresent()) {
            final RecurringRule matchingRule = recurringRule.orElseThrow();
            return resolved(date, matchingRule.status(), matchingRule.officeId());
        }

        final ScheduleDay scheduleDay = Objects.requireNonNull(
                defaultSchedule.days().get(date.getDayOfWeek()), "default schedule day");
        return resolved(date, scheduleDay.status(), scheduleDay.officeId());
    }

    public static Optional<ResolvedScheduleDay> findNextWorkingDay(
            final LocalDate today,
            final WorkSchedule defaultSchedule,
            final Collection<RecurringRule> recurringRules,
            final Collection<OneOffOverride> overrides) {
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(defaultSchedule, "defaultSchedule");
        Objects.requireNonNull(recurringRules, "recurringRules");
        Objects.requireNonNull(overrides, "overrides");

        if (!hasPotentialWorkingDate(today, defaultSchedule, recurringRules, overrides)) {
            return Optional.empty();
        }

        final LocalDate tomorrow = today.plusDays(1);
        final LocalDate searchEnd = searchEnd(today, defaultSchedule, recurringRules, overrides);
        LocalDate date = tomorrow;
        while (!date.isAfter(searchEnd)) {
            final ResolvedScheduleDay resolved = resolve(date, defaultSchedule, recurringRules, overrides);
            if (resolved.status() != ScheduleStatus.NON_WORKING) {
                return Optional.of(resolved);
            }
            date = date.plusDays(1);
        }
        return Optional.empty();
    }

    private static ResolvedScheduleDay resolved(
            final LocalDate date, final ScheduleStatus status, @Nullable final UUID officeId) {
        return new ResolvedScheduleDay(date, status, officeId);
    }

    private static boolean hasPotentialWorkingDate(
            final LocalDate today,
            final WorkSchedule defaultSchedule,
            final Collection<RecurringRule> recurringRules,
            final Collection<OneOffOverride> overrides) {
        final boolean defaultCanWork = defaultSchedule.days().values().stream()
                .anyMatch(day -> day.status() != ScheduleStatus.NON_WORKING);
        final boolean recurringCanWork = recurringRules.stream()
                .filter(rule -> rule.status() != ScheduleStatus.NON_WORKING)
                .anyMatch(rule -> rule.endDate() == null || rule.endDate().isAfter(today));
        final boolean overrideCanWork = overrides.stream()
                .filter(override -> override.status() != ScheduleStatus.NON_WORKING)
                .anyMatch(override -> override.effectiveEndDate().isAfter(today));
        return defaultCanWork || recurringCanWork || overrideCanWork;
    }

    private static LocalDate searchEnd(
            final LocalDate today,
            final WorkSchedule defaultSchedule,
            final Collection<RecurringRule> recurringRules,
            final Collection<OneOffOverride> overrides) {
        final boolean hasUnboundedWorkingSource = defaultSchedule.days().values().stream()
                .anyMatch(day -> day.status() != ScheduleStatus.NON_WORKING)
                || recurringRules.stream()
                        .anyMatch(rule -> rule.status() != ScheduleStatus.NON_WORKING && rule.endDate() == null);
        if (hasUnboundedWorkingSource) {
            return today.plusDays(MAX_SEARCH_DAYS);
        }

        return Stream.concat(
                        overrides.stream()
                .filter(override -> override.status() != ScheduleStatus.NON_WORKING)
                .map(OneOffOverride::effectiveEndDate)
                .filter(date -> date.isAfter(today)),
                        recurringRules.stream()
                        .filter(rule -> rule.status() != ScheduleStatus.NON_WORKING)
                        .map(RecurringRule::endDate)
                        .filter(Objects::nonNull)
                        .filter(date -> date.isAfter(today)))
                .max(Comparator.naturalOrder())
                .orElse(today);
    }
}
