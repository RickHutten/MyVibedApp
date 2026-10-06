package nl.codestar.myvibedapp.schedule.domain;

import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ScheduleDay(ScheduleStatus status, @Nullable UUID officeId) {

    public ScheduleDay {
        Objects.requireNonNull(status, "status");
    }
}
