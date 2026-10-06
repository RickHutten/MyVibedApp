package nl.codestar.myvibedapp.schedule.application;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.schedule.application.GeocodingPort.AddressSuggestion;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.ScheduleDay;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final ScheduleStore scheduleStore;
    private final GeocodingPort geocodingPort;

    @Transactional(readOnly = true)
    public WorkSchedule getSchedule() {
        return scheduleStore.schedule();
    }

    @Transactional
    public WorkSchedule replaceSchedule(final WorkSchedule schedule) {
        validateOfficeAssignments(schedule);
        scheduleStore.saveSchedule(schedule);
        return schedule;
    }

    @Transactional(readOnly = true)
    public List<SavedOffice> offices(final boolean includeDeleted) {
        return scheduleStore.offices(includeDeleted);
    }

    public List<AddressSuggestion> searchOffices(final String query) {
        if (query.isBlank()) {
            return List.of();
        }
        return geocodingPort.search(query);
    }

    @Transactional
    public SavedOffice addOffice(final String label, final String address, final double latitude, final double longitude) {
        return scheduleStore.saveOffice(new SavedOffice(UUID.randomUUID(), label, address, latitude, longitude, false));
    }

    @Transactional
    public SavedOffice editOffice(final UUID id, final String label, final String address, final double latitude, final double longitude) {
        final SavedOffice existing = findOffice(id);
        return scheduleStore.saveOffice(existing.update(label, address, latitude, longitude));
    }

    @Transactional
    public void softDeleteOffice(final UUID id) {
        final SavedOffice existing = findOffice(id);
        if (existing.deleted()) {
            return;
        }
        scheduleStore.saveOffice(existing.softDelete());
    }

    private void validateOfficeAssignments(final WorkSchedule schedule) {
        final WorkSchedule current = scheduleStore.schedule();
        schedule.days().forEach((day, scheduleDay) -> {
            if (scheduleDay.status() != ScheduleStatus.OFFICE) {
                return;
            }
            final UUID officeId = Objects.requireNonNull(scheduleDay.officeId(), "An office day requires an office");
            final SavedOffice office = findOffice(officeId);
            final ScheduleDay previous = current.days().get(day);
            final boolean existingAssignment = previous != null && officeId.equals(previous.officeId());
            if (office.deleted() && !existingAssignment) {
                throw new IllegalArgumentException("A deleted office cannot be selected for a new assignment");
            }
        });
    }

    private SavedOffice findOffice(final UUID id) {
        return scheduleStore.offices(true).stream()
                .filter(office -> office.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Office was not found"));
    }
}
