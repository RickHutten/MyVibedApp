package nl.codestar.myvibedapp.commute.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import nl.codestar.myvibedapp.commute.domain.TransitOrigin;

public record TransitRequest(
        TransitOrigin origin,
        double destinationLatitude,
        double destinationLongitude,
        LocalDate date,
        LocalDateTime targetArrival) {

    public TransitRequest {
        if (!Double.isFinite(destinationLatitude) || destinationLatitude < -90 || destinationLatitude > 90) {
            throw new IllegalArgumentException("Destination latitude must be between -90 and 90");
        }
        if (!Double.isFinite(destinationLongitude) || destinationLongitude < -180 || destinationLongitude > 180) {
            throw new IllegalArgumentException("Destination longitude must be between -180 and 180");
        }
    }
}
