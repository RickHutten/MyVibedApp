package nl.codestar.myvibedapp.schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.domain.OneOffOverride;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OneOffOverrideServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");
    private static final Clock TODAY = Clock.fixed(
            Instant.parse("2026-10-07T10:00:00Z"), ZONE);

    private InMemoryScheduleStore store;
    private OneOffOverrideService service;

    @BeforeEach
    void setUp() {
        store = new InMemoryScheduleStore();
        service = new OneOffOverrideService(store, TODAY);
    }

    @Test
    void addsOverrideAndListsIt() {
        final OneOffOverride override = override(
                LocalDate.of(2026, 10, 8), null, ScheduleStatus.NON_WORKING, null);

        assertThat(service.add(override)).isEqualTo(override);
        assertThat(service.overrides()).containsExactly(override);
    }

    @Test
    void editsExistingOverrideAndPreservesItsIdentifier() {
        final OneOffOverride existing = override(
                LocalDate.of(2026, 10, 8), null, ScheduleStatus.NON_WORKING, null);
        final OneOffOverride replacement = override(
                LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 10), ScheduleStatus.WORK_FROM_HOME, null);
        store.overrides.add(existing);

        final OneOffOverride updated = service.edit(existing.id(), replacement);

        assertThat(updated.id()).isEqualTo(existing.id());
        assertThat(updated.startDate()).isEqualTo(replacement.startDate());
        assertThat(updated.endDate()).isEqualTo(replacement.endDate());
        assertThat(updated.status()).isEqualTo(replacement.status());
        assertThat(service.overrides()).containsExactly(updated);
    }

    @Test
    void rejectsCreatingAnOverrideInThePast() {
        final OneOffOverride override = override(
                LocalDate.of(2026, 10, 6), null, ScheduleStatus.NON_WORKING, null);

        assertThatThrownBy(() -> service.add(override))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An override start date cannot be in the past");
        assertThat(store.overrides).isEmpty();
    }

    @Test
    void rejectsOverlappingRangeWithoutChangingExistingOverride() {
        final OneOffOverride existing = override(
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10), ScheduleStatus.NON_WORKING, null);
        store.overrides.add(existing);
        final OneOffOverride overlapping = override(
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12), ScheduleStatus.WORK_FROM_HOME, null);

        assertThatThrownBy(() -> service.add(overlapping))
                .isInstanceOf(OneOffOverrideConflictException.class)
                .extracting(exception -> ((OneOffOverrideConflictException) exception).conflictingOverrideId())
                .isEqualTo(existing.id());
        assertThat(store.overrides).containsExactly(existing);
    }

    @Test
    void rejectsRangeThatContainsAnExistingOverride() {
        final OneOffOverride existing = override(
                LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 10), ScheduleStatus.NON_WORKING, null);
        store.overrides.add(existing);
        final OneOffOverride containing = override(
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 11), ScheduleStatus.WORK_FROM_HOME, null);

        assertThatThrownBy(() -> service.add(containing))
                .isInstanceOf(OneOffOverrideConflictException.class)
                .extracting(exception -> ((OneOffOverrideConflictException) exception).conflictingOverrideId())
                .isEqualTo(existing.id());
        assertThat(store.overrides).containsExactly(existing);
    }

    @Test
    void allowsAdjacentRanges() {
        final OneOffOverride existing = override(
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10), ScheduleStatus.NON_WORKING, null);
        store.overrides.add(existing);
        final OneOffOverride adjacent = override(
                LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 12), ScheduleStatus.WORK_FROM_HOME, null);

        service.add(adjacent);

        assertThat(store.overrides).containsExactly(existing, adjacent);
    }

    @Test
    void rejectsChangingRangeToOverlapAndLeavesBothOverridesUnchanged() {
        final OneOffOverride existing = override(
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10), ScheduleStatus.NON_WORKING, null);
        final OneOffOverride other = override(
                LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 13), ScheduleStatus.WORK_FROM_HOME, null);
        store.overrides.add(existing);
        store.overrides.add(other);

        final OneOffOverride replacement = override(
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12), ScheduleStatus.NON_WORKING, null);

        assertThatThrownBy(() -> service.edit(existing.id(), replacement))
                .isInstanceOf(OneOffOverrideConflictException.class)
                .extracting(exception -> ((OneOffOverrideConflictException) exception).conflictingOverrideId())
                .isEqualTo(other.id());
        assertThat(store.overrides).containsExactly(existing, other);
    }

    @Test
    void allowsDeletingPastOverrideButDoesNotAllowEditingIt() {
        final OneOffOverride existing = override(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 7), ScheduleStatus.NON_WORKING, null);
        store.overrides.add(existing);

        assertThatThrownBy(() -> service.edit(existing.id(), override(
                existing.startDate(), existing.endDate(), ScheduleStatus.WORK_FROM_HOME, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A past override cannot be edited");

        service.delete(existing.id());

        assertThat(store.overrides).isEmpty();
    }

    @Test
    void rejectsAddingOverrideForUnknownOffice() {
        final OneOffOverride officeOverride = override(
                LocalDate.of(2026, 10, 8), null, ScheduleStatus.OFFICE, UUID.randomUUID());

        assertThatThrownBy(() -> service.add(officeOverride))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Office was not found");
    }

    @Test
    void rejectsAddingOverrideForDeletedOffice() {
        final UUID officeId = UUID.randomUUID();
        store.offices.add(new SavedOffice(officeId, "Old office", "Address", 52, 4, true));
        final OneOffOverride officeOverride = override(
                LocalDate.of(2026, 10, 8), null, ScheduleStatus.OFFICE, officeId);

        assertThatThrownBy(() -> service.add(officeOverride))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A deleted office cannot be selected for a new override");
        assertThat(store.overrides).isEmpty();
    }

    @Test
    void allowsEditingOverrideThatKeepsItsDeletedOfficeAssignment() {
        final UUID officeId = UUID.randomUUID();
        final OneOffOverride existing = override(
                LocalDate.of(2026, 10, 8), null, ScheduleStatus.OFFICE, officeId);
        final OneOffOverride replacement = override(
                LocalDate.of(2026, 10, 9), null, ScheduleStatus.OFFICE, officeId);
        store.offices.add(new SavedOffice(officeId, "Old office", "Address", 52, 4, true));
        store.overrides.add(existing);

        final OneOffOverride updated = service.edit(existing.id(), replacement);

        assertThat(updated.id()).isEqualTo(existing.id());
        assertThat(updated.startDate()).isEqualTo(replacement.startDate());
        assertThat(updated.officeId()).isEqualTo(officeId);
    }

    @Test
    void rejectsEditingToAPastStartDate() {
        final OneOffOverride existing = override(
                LocalDate.of(2026, 10, 8), null, ScheduleStatus.NON_WORKING, null);
        final OneOffOverride replacement = override(
                LocalDate.of(2026, 10, 6), null, ScheduleStatus.WORK_FROM_HOME, null);
        store.overrides.add(existing);

        assertThatThrownBy(() -> service.edit(existing.id(), replacement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An override start date cannot be in the past");
        assertThat(store.overrides).containsExactly(existing);
    }

    @Test
    void rejectsEditingMissingOverride() {
        final OneOffOverride replacement = override(
                LocalDate.of(2026, 10, 8), null, ScheduleStatus.NON_WORKING, null);

        assertThatThrownBy(() -> service.edit(UUID.randomUUID(), replacement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("One-off override was not found");
    }

    @Test
    void rejectsDeletingMissingOverride() {
        assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("One-off override was not found");
    }

    private static OneOffOverride override(
            final LocalDate startDate,
            final @Nullable LocalDate endDate,
            final ScheduleStatus status,
            final @Nullable UUID officeId) {
        return new OneOffOverride(UUID.randomUUID(), startDate, endDate, status, officeId);
    }

    private static final class InMemoryScheduleStore implements ScheduleStore {

        private final List<SavedOffice> offices = new ArrayList<>();
        private final List<OneOffOverride> overrides = new ArrayList<>();

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
            return List.of();
        }

        @Override
        public RecurringRule saveRecurringRule(final RecurringRule rule) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteRecurringRule(final UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<OneOffOverride> oneOffOverrides() {
            return List.copyOf(overrides);
        }

        @Override
        public OneOffOverride saveOneOffOverride(final OneOffOverride override) {
            overrides.removeIf(current -> current.id().equals(override.id()));
            overrides.add(override);
            return override;
        }

        @Override
        public void deleteOneOffOverride(final UUID id) {
            overrides.removeIf(current -> current.id().equals(id));
        }
    }
}
