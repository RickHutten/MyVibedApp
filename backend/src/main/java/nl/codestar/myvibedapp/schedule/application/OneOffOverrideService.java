package nl.codestar.myvibedapp.schedule.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.schedule.domain.OneOffOverride;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OneOffOverrideService {

    private final ScheduleStore scheduleStore;
    private final Clock scheduleClock;

    @Transactional(readOnly = true)
    public List<OneOffOverride> overrides() {
        return scheduleStore.oneOffOverrides();
    }

    @Transactional
    public OneOffOverride add(final OneOffOverride override) {
        ensureStartDateIsCurrentOrFuture(override.startDate());
        validateOffice(override, null);
        ensureRangeIsAvailable(override, null);
        return scheduleStore.saveOneOffOverride(override);
    }

    @Transactional
    public OneOffOverride edit(final UUID id, final OneOffOverride replacement) {
        final OneOffOverride existing = find(id);
        final LocalDate today = today();
        if (existing.startDate().isBefore(today)) {
            throw new IllegalArgumentException("A past override cannot be edited");
        }
        ensureStartDateIsCurrentOrFuture(replacement.startDate());
        final OneOffOverride updated = new OneOffOverride(
                id, replacement.startDate(), replacement.endDate(), replacement.status(), replacement.officeId());
        validateOffice(updated, existing);
        ensureRangeIsAvailable(updated, id);
        return scheduleStore.saveOneOffOverride(updated);
    }

    @Transactional
    public void delete(final UUID id) {
        find(id);
        scheduleStore.deleteOneOffOverride(id);
    }

    private void ensureStartDateIsCurrentOrFuture(final LocalDate startDate) {
        if (startDate.isBefore(today())) {
            throw new IllegalArgumentException("An override start date cannot be in the past");
        }
    }

    private void ensureRangeIsAvailable(final OneOffOverride candidate, @Nullable final UUID excludedId) {
        scheduleStore.oneOffOverrides().stream()
                .filter(override -> rangesOverlap(override, candidate))
                .filter(override -> !override.id().equals(excludedId))
                .findFirst()
                .ifPresent(override -> {
                    throw new OneOffOverrideConflictException(override.id());
                });
    }

    private boolean rangesOverlap(final OneOffOverride first, final OneOffOverride second) {
        return !first.startDate().isAfter(second.effectiveEndDate())
                && !second.startDate().isAfter(first.effectiveEndDate());
    }

    private void validateOffice(final OneOffOverride override, @Nullable final OneOffOverride existing) {
        final UUID officeId = override.officeId();
        if (officeId == null) {
            return;
        }
        final SavedOffice office = scheduleStore.offices(true).stream()
                .filter(candidate -> candidate.id().equals(officeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Office was not found"));
        final boolean existingAssignment = existing != null && officeId.equals(existing.officeId());
        if (office.deleted() && !existingAssignment) {
            throw new IllegalArgumentException("A deleted office cannot be selected for a new override");
        }
    }

    private OneOffOverride find(final UUID id) {
        return scheduleStore.oneOffOverrides().stream()
                .filter(override -> override.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("One-off override was not found"));
    }

    private LocalDate today() {
        return LocalDate.now(scheduleClock);
    }
}
