package nl.codestar.myvibedapp.schedule.domain;

import java.time.LocalTime;
import java.util.Objects;

public record WorkingHours(LocalTime start, LocalTime end) {

    public WorkingHours {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Working-hours start must be before end");
        }
    }
}
