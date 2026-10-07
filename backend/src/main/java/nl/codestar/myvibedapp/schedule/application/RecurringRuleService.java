package nl.codestar.myvibedapp.schedule.application;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.RecurringRuleConflictChecker;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecurringRuleService {

    private final ScheduleStore scheduleStore;

    @Transactional(readOnly = true)
    public List<RecurringRule> rules() {
        return scheduleStore.recurringRules();
    }

    @Transactional
    public RecurringRule add(final RecurringRule rule) {
        validateOffice(rule, null);
        ensureNoConflict(rule);
        return scheduleStore.saveRecurringRule(rule);
    }

    @Transactional
    public RecurringRule edit(final UUID id, final RecurringRule replacement) {
        final RecurringRule existing = find(id);
        final RecurringRule updated = new RecurringRule(
                id,
                replacement.level(),
                replacement.interval(),
                replacement.weekdays(),
                replacement.monthlyPattern(),
                replacement.startDate(),
                replacement.endDate(),
                replacement.status(),
                replacement.officeId());
        validateOffice(updated, existing);
        ensureNoConflict(updated);
        return scheduleStore.saveRecurringRule(updated);
    }

    @Transactional
    public void delete(final UUID id) {
        find(id);
        scheduleStore.deleteRecurringRule(id);
    }

    private void ensureNoConflict(final RecurringRule candidate) {
        RecurringRuleConflictChecker.findConflict(candidate, scheduleStore.recurringRules())
                .ifPresent(conflict -> {
                    throw new RecurringRuleConflictException(conflict.id());
                });
    }

    private void validateOffice(final RecurringRule rule, @Nullable final RecurringRule existing) {
        final UUID officeId = rule.officeId();
        if (officeId == null) {
            return;
        }
        final SavedOffice office = scheduleStore.offices(true).stream()
                .filter(candidate -> candidate.id().equals(officeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Office was not found"));
        final boolean existingAssignment = existing != null && officeId.equals(existing.officeId());
        if (office.deleted() && !existingAssignment) {
            throw new IllegalArgumentException("A deleted office cannot be selected for a new rule");
        }
    }

    private RecurringRule find(final UUID id) {
        return scheduleStore.recurringRules().stream()
                .filter(rule -> rule.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Recurring rule was not found"));
    }
}
