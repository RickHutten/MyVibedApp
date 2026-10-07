package nl.codestar.myvibedapp.schedule.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.domain.RecurrenceLevel;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;
import org.junit.jupiter.api.Test;

class RecurringRuleServiceTest {

    @Test
    void rejectsOfficeRuleUsingDeletedOffice() {
        final UUID officeId = UUID.randomUUID();
        final ScheduleStore store = new InMemoryScheduleStore(
                List.of(new SavedOffice(
                        officeId, "Old office", "Address", 52, 4, true)));
        final RecurringRuleService service = new RecurringRuleService(store);
        final RecurringRule officeRule = new RecurringRule(
                UUID.randomUUID(), RecurrenceLevel.DAYS, 1, Set.of(), null,
                LocalDate.of(2026, 1, 1), null, ScheduleStatus.OFFICE, officeId);

        assertThatThrownBy(() -> service.add(officeRule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deleted office");
    }

    private static final class InMemoryScheduleStore implements ScheduleStore {
        private final List<SavedOffice> offices;

        private InMemoryScheduleStore(final List<SavedOffice> offices) {
            this.offices = offices;
        }

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
            return offices;
        }

        @Override
        public SavedOffice saveOffice(
                final SavedOffice office) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RecurringRule> recurringRules() {
            return List.of();
        }

        @Override
        public RecurringRule saveRecurringRule(final RecurringRule rule) {
            return rule;
        }

        @Override
        public void deleteRecurringRule(final UUID id) {
            throw new UnsupportedOperationException();
        }
    }
}
