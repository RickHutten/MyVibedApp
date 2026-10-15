package nl.codestar.myvibedapp.commute.adapters.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import nl.codestar.myvibedapp.commute.application.TodayCommute;
import nl.codestar.myvibedapp.commute.domain.JourneySegment;
import nl.codestar.myvibedapp.commute.domain.PublicTransportSegment;
import nl.codestar.myvibedapp.commute.domain.WalkingSegment;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
record TodayCommuteResponse(
        TodayCommute.Target target,
        TodayCommute.Status status,
        @Nullable LocalDate date,
        @Nullable LocalTime wakeUpTime,
        @Nullable LocalTime leaveHomeTime,
        @Nullable LocalTime plannedArrivalTime,
        @Nullable String destinationOffice,
        @Nullable Long totalDurationMinutes,
        @Nullable Integer transferCount,
        List<JourneySegmentResponse> segments) {

    static TodayCommuteResponse from(final TodayCommute commute) {
        return new TodayCommuteResponse(
                commute.target(),
                commute.status(),
                commute.date(),
                commute.wakeUpTime(),
                commute.leaveHomeTime(),
                commute.plannedArrivalTime(),
                commute.destinationOffice(),
                commute.totalDurationMinutes(),
                commute.transferCount(),
                commute.segments().stream().map(TodayCommuteResponse::toSegment).toList());
    }

    private static JourneySegmentResponse toSegment(final JourneySegment segment) {
        return switch (segment) {
            case WalkingSegment(
                    LocalDateTime departure, LocalDateTime arrival, Optional<String> from, Optional<String> destination
            ) -> new JourneySegmentResponse(
                    "WALKING",
                    null,
                    null,
                    null,
                    null,
                    departure.toLocalTime(),
                    arrival.toLocalTime(),
                    from.orElse(null),
                    destination.orElse(null),
                    null
            );
            case PublicTransportSegment(
                    int transferNumber, Optional<String> mode, Optional<String> service, Optional<String> direction,
                    LocalDateTime departure, LocalDateTime arrival, Optional<String> from, Optional<String> destination, Optional<String> platform
            ) -> new JourneySegmentResponse(
                    "PUBLIC_TRANSPORT",
                    transferNumber,
                    mode.orElse(null),
                    service.orElse(null),
                    direction.orElse(null),
                    departure.toLocalTime(),
                    arrival.toLocalTime(),
                    from.orElse(null),
                    destination.orElse(null),
                    platform.orElse(null));
        };
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record JourneySegmentResponse(
            String type,
            @Nullable Integer transferNumber,
            @Nullable String mode,
            @Nullable String service,
            @Nullable String direction,
            LocalTime departureTime,
            LocalTime arrivalTime,
            @Nullable String from,
            @Nullable String to,
            @Nullable String platform) {
    }
}
