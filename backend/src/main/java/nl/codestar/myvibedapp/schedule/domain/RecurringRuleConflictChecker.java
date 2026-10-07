package nl.codestar.myvibedapp.schedule.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public final class RecurringRuleConflictChecker {

    private static final long MAX_UNBOUNDED_SEARCH_DAYS = 366L * 400L;

    private RecurringRuleConflictChecker() {
    }

    public static Optional<RecurringRule> findConflict(
            final RecurringRule candidate,
            final Collection<RecurringRule> existingRules) {
        return existingRules.stream()
                .filter(existing -> existing.level() == candidate.level())
                .filter(existing -> !existing.id().equals(candidate.id()))
                .filter(existing -> overlaps(candidate, existing))
                .findFirst();
    }

    private static boolean overlaps(final RecurringRule first, final RecurringRule second) {
        final LocalDate start = first.startDate().isAfter(second.startDate()) ? first.startDate() : second.startDate();
        final LocalDate boundedEnd = boundedEnd(first, second, start);
        if (boundedEnd.isBefore(start)) {
            return false;
        }
        LocalDate date = start;
        while (!date.isAfter(boundedEnd)) {
            if (RecurringRuleEvaluator.matches(first, date) && RecurringRuleEvaluator.matches(second, date)) {
                return true;
            }
            date = date.plusDays(1);
        }
        return false;
    }

    private static LocalDate boundedEnd(
            final RecurringRule first,
            final RecurringRule second,
            final LocalDate start) {
        final LocalDate firstEnd = first.endDate();
        final LocalDate secondEnd = second.endDate();
        if (firstEnd != null && secondEnd != null) {
            return firstEnd.isBefore(secondEnd) ? firstEnd : secondEnd;
        }
        if (firstEnd != null) {
            return firstEnd;
        }
        if (secondEnd != null) {
            return secondEnd;
        }
        return start.plusDays(MAX_UNBOUNDED_SEARCH_DAYS);
    }
}
