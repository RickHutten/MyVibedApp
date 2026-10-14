package nl.codestar.myvibedapp.commute.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public sealed interface JourneySegment permits WalkingSegment, PublicTransportSegment {

    LocalDateTime departure();

    LocalDateTime arrival();

    Optional<String> from();

    Optional<String> destination();
}
