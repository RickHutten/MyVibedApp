package nl.codestar.myvibedapp.commute.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public record PublicTransportSegment(
        int transferNumber,
        Optional<String> mode,
        Optional<String> service,
        Optional<String> direction,
        LocalDateTime departure,
        LocalDateTime arrival,
        Optional<String> from,
        Optional<String> destination,
        Optional<String> platform) implements JourneySegment {

    public PublicTransportSegment {
        if (transferNumber < 0) {
            throw new IllegalArgumentException("Transfer number must not be negative");
        }
        if (arrival.isBefore(departure)) {
            throw new IllegalArgumentException("Public transport segment arrival must not precede departure");
        }
        mode = clean(mode);
        service = clean(service);
        direction = clean(direction);
        from = clean(from);
        destination = clean(destination);
        platform = clean(platform);
    }

    private static Optional<String> clean(final Optional<String> value) {
        return value.filter(text -> !text.isBlank());
    }
}
