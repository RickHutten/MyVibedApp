package nl.codestar.myvibedapp.schedule.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ResolvedScheduleDay(LocalDate date, ScheduleStatus status, @Nullable UUID officeId) {

    public ResolvedScheduleDay {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(status, "status");
        if (status == ScheduleStatus.OFFICE && officeId == null) {
            throw new IllegalArgumentException("An office day requires an office");
        }
        if (status != ScheduleStatus.OFFICE && officeId != null) {
            throw new IllegalArgumentException("Only office days can reference an office");
        }
    }
}
