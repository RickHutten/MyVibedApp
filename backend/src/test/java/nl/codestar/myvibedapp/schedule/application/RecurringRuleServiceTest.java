package nl.codestar.myvibedapp.schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.domain.OneOffOverride;
import nl.codestar.myvibedapp.schedule.domain.RecurrenceLevel;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class RecurringRuleServiceTest {

    private final InMemoryScheduleStore store = new InMemoryScheduleStore();
    private final RecurringRuleService service = new RecurringRuleService(store);

    @Test
    void rejectsOfficeRuleUsingDeletedOffice() {
        final UUID officeId = UUID.randomUUID();
        store.offices.add(new SavedOffice(officeId, "Old office", "Address", 52, 4, true));
        final RecurringRule officeRule = officeRule(officeId, LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> service.add(officeRule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deleted office");
    }

    @Test
    void rejectsOfficeRuleUsingUnknownOffice() {
        final RecurringRule officeRule = officeRule(UUID.randomUUID(), LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> service.add(officeRule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Office was not found");
    }

    @Test
    void addsRuleAndListsIt() {
        final RecurringRule rule = dailyRule(1, LocalDate.of(2026, 1, 1), null);

        assertThat(service.add(rule)).isEqualTo(rule);
        assertThat(service.rules()).containsExactly(rule);
    }

    @Test
    void rejectsAddingRuleWithAnActualOccurrenceConflict() {
        final RecurringRule existing = dailyRule(2, LocalDate.of(2026, 1, 1), null);
        final RecurringRule candidate = dailyRule(3, LocalDate.of(2026, 1, 5), null);
        store.recurringRules.add(existing);

        assertThatThrownBy(() -> service.add(candidate))
                .isInstanceOf(RecurringRuleConflictException.class)
                .extracting(exception -> ((RecurringRuleConflictException) exception).conflictingRuleId())
                .isEqualTo(existing.id());
        assertThat(service.rules()).containsExactly(existing);
    }

    @Test
    void editsExistingRuleAndPreservesItsIdentifier() {
        final RecurringRule existing = dailyRule(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));
        final RecurringRule replacement = weeklyRule(1, Set.of(DayOfWeek.MONDAY), LocalDate.of(2026, 2, 1));
        store.recurringRules.add(existing);

        final RecurringRule updated = service.edit(existing.id(), replacement);

        assertThat(updated.id()).isEqualTo(existing.id());
        assertThat(updated.level()).isEqualTo(replacement.level());
        assertThat(updated.weekdays()).isEqualTo(replacement.weekdays());
        assertThat(updated.startDate()).isEqualTo(replacement.startDate());
        assertThat(service.rules()).containsExactly(updated);
    }

    @Test
    void rejectsEditingRuleWhenReplacementConflictsWithAnotherRule() {
        final RecurringRule existing = dailyRule(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3));
        final RecurringRule other = dailyRule(2, LocalDate.of(2026, 1, 10), null);
        final RecurringRule replacement = dailyRule(1, LocalDate.of(2026, 1, 10), null);
        store.recurringRules.add(existing);
        store.recurringRules.add(other);

        assertThatThrownBy(() -> service.edit(existing.id(), replacement))
                .isInstanceOf(RecurringRuleConflictException.class)
                .extracting(exception -> ((RecurringRuleConflictException) exception).conflictingRuleId())
                .isEqualTo(other.id());
        assertThat(service.rules()).containsExactly(existing, other);
    }

    @Test
    void allowsEditingRuleThatKeepsItsDeletedOfficeAssignment() {
        final UUID officeId = UUID.randomUUID();
        final RecurringRule existing = officeRule(officeId, LocalDate.of(2026, 1, 1));
        final RecurringRule replacement = officeRule(officeId, LocalDate.of(2026, 2, 1));
        store.offices.add(new SavedOffice(officeId, "Old office", "Address", 52, 4, true));
        store.recurringRules.add(existing);

        final RecurringRule updated = service.edit(existing.id(), replacement);

        assertThat(updated.id()).isEqualTo(existing.id());
        assertThat(updated.startDate()).isEqualTo(replacement.startDate());
        assertThat(updated.officeId()).isEqualTo(officeId);
    }

    @Test
    void deletesExistingRule() {
        final RecurringRule existing = dailyRule(1, LocalDate.of(2026, 1, 1), null);
        store.recurringRules.add(existing);

        service.delete(existing.id());

        assertThat(service.rules()).isEmpty();
    }

    @Test
    void rejectsEditingMissingRule() {
        final RecurringRule replacement = dailyRule(1, LocalDate.of(2026, 1, 1), null);

        assertThatThrownBy(() -> service.edit(UUID.randomUUID(), replacement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Recurring rule was not found");
    }

    @Test
    void rejectsDeletingMissingRule() {
        assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Recurring rule was not found");
    }

    private static RecurringRule dailyRule(
            final int interval,
            final LocalDate start,
            final @Nullable LocalDate end) {
        return new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.DAYS, interval, Set.of(), null, start, end,
                ScheduleStatus.WORK_FROM_HOME, null);
    }

    private static RecurringRule weeklyRule(
            final int interval,
            final Set<DayOfWeek> weekdays,
            final LocalDate start) {
        return new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.WEEKS, interval, weekdays, null, start, null,
                ScheduleStatus.WORK_FROM_HOME, null);
    }

    private static RecurringRule officeRule(final UUID officeId, final LocalDate start) {
        return new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.DAYS, 1, Set.of(), null, start, null,
                ScheduleStatus.OFFICE, officeId);
    }

    private static final class InMemoryScheduleStore implements ScheduleStore {

        private final List<SavedOffice> offices = new ArrayList<>();
        private final List<RecurringRule> recurringRules = new ArrayList<>();

        @Override
        public WorkSchedule schedule() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveSchedule(final WorkSchedule schedule) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<SavedOffice> offices(final boolean includeDeleted) {
            return List.copyOf(offices);
        }

        @Override
        public SavedOffice saveOffice(final SavedOffice office) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RecurringRule> recurringRules() {
            return List.copyOf(recurringRules);
        }

        @Override
        public RecurringRule saveRecurringRule(final RecurringRule rule) {
            recurringRules.removeIf(existing -> existing.id().equals(rule.id()));
            recurringRules.add(rule);
            return rule;
        }

        @Override
        public void deleteRecurringRule(final UUID id) {
            recurringRules.removeIf(existing -> existing.id().equals(id));
        }

        @Override
        public List<OneOffOverride> oneOffOverrides() {
            return List.of();
        }

        @Override
        public OneOffOverride saveOneOffOverride(final OneOffOverride override) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteOneOffOverride(final UUID id) {
            throw new UnsupportedOperationException();
        }
    }
}
