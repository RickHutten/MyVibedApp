package nl.codestar.myvibedapp.commute.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import nl.codestar.myvibedapp.commute.domain.JourneySegment;
import org.jspecify.annotations.Nullable;

public record TodayCommute(
        Status status,
        @Nullable LocalDate date,
        @Nullable LocalTime wakeUpTime,
        @Nullable LocalTime leaveHomeTime,
        @Nullable LocalTime plannedArrivalTime,
        @Nullable String destinationOffice,
        @Nullable Long totalDurationMinutes,
        @Nullable Integer transferCount,
        List<JourneySegment> segments) {

    public TodayCommute {
        segments = List.copyOf(segments);
        if (status != Status.AVAILABLE && !segments.isEmpty()) {
            throw new IllegalArgumentException("Only an available commute can contain route segments");
        }
    }

    public static TodayCommute hidden() {
        return new TodayCommute(Status.HIDDEN, null, null, null, null, null, null, null, List.of());
    }

    public static TodayCommute unavailable() {
        return new TodayCommute(Status.UNAVAILABLE, null, null, null, null, null, null, null, List.of());
    }

    public enum Status {
        HIDDEN,
        UNAVAILABLE,
        AVAILABLE
    }
}
