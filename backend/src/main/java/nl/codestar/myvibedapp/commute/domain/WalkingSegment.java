package nl.codestar.myvibedapp.commute.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public record WalkingSegment(
        LocalDateTime departure,
        LocalDateTime arrival,
        Optional<String> from,
        Optional<String> destination) implements JourneySegment {

    public WalkingSegment {
        if (arrival.isBefore(departure)) {
            throw new IllegalArgumentException("Walking segment arrival must not precede departure");
        }
        from = from.filter(value -> !value.isBlank());
        destination = destination.filter(value -> !value.isBlank());
    }
}
