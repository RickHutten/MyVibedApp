package nl.codestar.myvibedapp.commute.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record TransitJourney(
        LocalDateTime departure,
        LocalDateTime arrival,
        List<JourneySegment> segments) {

    public TransitJourney {
        if (arrival.isBefore(departure)) {
            throw new IllegalArgumentException("Transit journey arrival must not precede departure");
        }
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("A transit journey requires at least one journey segment");
        }
        segments = List.copyOf(segments);
        if (segments.getFirst().departure().isBefore(departure)
                || segments.getLast().arrival().isAfter(arrival)) {
            throw new IllegalArgumentException("Journey segments must fit within the journey");
        }
        for (int index = 1; index < segments.size(); index++) {
            if (segments.get(index).departure().isBefore(segments.get(index - 1).arrival())) {
                throw new IllegalArgumentException("Journey segments must be chronological");
            }
        }
    }

    public Optional<PublicTransportSegment> firstPublicTransportSegment() {
        return segments.stream()
                .filter(PublicTransportSegment.class::isInstance)
                .map(PublicTransportSegment.class::cast)
                .findFirst();
    }

    public Duration totalDuration() {
        return Duration.between(departure, arrival);
    }

    public int transferCount() {
        return Math.max(0, (int) segments.stream()
                .filter(PublicTransportSegment.class::isInstance)
                .count() - 1);
    }
}
