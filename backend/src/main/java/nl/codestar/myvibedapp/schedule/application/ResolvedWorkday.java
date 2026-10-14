package nl.codestar.myvibedapp.schedule.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record ResolvedWorkday(
        LocalDate date,
        Status status,
        @Nullable OfficeLocation office,
        LocalTime workingHoursStart) {

    public ResolvedWorkday {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(workingHoursStart, "workingHoursStart");
        if (status == Status.OFFICE && office == null) {
            throw new IllegalArgumentException("An office day requires an office location");
        }
        if (status != Status.OFFICE && office != null) {
            throw new IllegalArgumentException("Only office days can contain an office location");
        }
    }

    public enum Status {
        OFFICE,
        WORK_FROM_HOME,
        NON_WORKING
    }

    public record OfficeLocation(String label, double latitude, double longitude) {

        public OfficeLocation {
            if (label.isBlank()) {
                throw new IllegalArgumentException("Office label must not be blank");
            }
            if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Office latitude must be between -90 and 90");
            }
            if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Office longitude must be between -180 and 180");
            }
        }
    }
}
