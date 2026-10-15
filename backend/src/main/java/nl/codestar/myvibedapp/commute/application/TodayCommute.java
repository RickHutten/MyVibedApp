package nl.codestar.myvibedapp.commute.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import nl.codestar.myvibedapp.commute.domain.JourneySegment;
import org.jspecify.annotations.Nullable;

public record TodayCommute(
        Target target,
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

    public static TodayCommute hidden(final Target target) {
        return new TodayCommute(target, Status.HIDDEN, null, null, null, null, null, null, null, List.of());
    }

    public static TodayCommute unavailable(final Target target) {
        return new TodayCommute(target, Status.UNAVAILABLE, null, null, null, null, null, null, null, List.of());
    }

    public static TodayCommute noUpcomingOfficeDay() {
        return new TodayCommute(
                Target.NEXT_OFFICE_DAY, Status.NO_UPCOMING_OFFICE_DAY,
                null, null, null, null, null, null, null, List.of());
    }

    public enum Status {
        HIDDEN,
        UNAVAILABLE,
        NO_UPCOMING_OFFICE_DAY,
        AVAILABLE
    }

    public enum Target {
        TODAY,
        NEXT_OFFICE_DAY
    }
}
