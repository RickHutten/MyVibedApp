package nl.codestar.myvibedapp.schedule.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record OneOffOverride(
        UUID id,
        LocalDate startDate,
        @Nullable LocalDate endDate,
        ScheduleStatus status,
        @Nullable UUID officeId) {

    public OneOffOverride {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(status, "status");
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Override end date cannot be before its start date");
        }
        if (status == ScheduleStatus.OFFICE && officeId == null) {
            throw new IllegalArgumentException("An office override requires an office");
        }
        if (status != ScheduleStatus.OFFICE && officeId != null) {
            throw new IllegalArgumentException("Only office overrides can reference an office");
        }
    }

    public boolean covers(final LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(effectiveEndDate());
    }

    public LocalDate effectiveEndDate() {
        return endDate == null ? startDate : endDate;
    }
}
