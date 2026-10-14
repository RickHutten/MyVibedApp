package nl.codestar.myvibedapp.commute.application;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.codestar.myvibedapp.commute.domain.CommutePreferences;
import nl.codestar.myvibedapp.commute.domain.PublicTransportSegment;
import nl.codestar.myvibedapp.commute.domain.TransitJourney;
import nl.codestar.myvibedapp.commute.domain.TransitOrigin;
import nl.codestar.myvibedapp.commute.domain.WalkingSegment;
import nl.codestar.myvibedapp.schedule.application.ResolvedWorkday;
import nl.codestar.myvibedapp.schedule.application.UpcomingWorkScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodayCommuteService {

    private final CommutePreferencesService commutePreferencesService;
    private final UpcomingWorkScheduleService upcomingWorkScheduleService;
    private final TransitProvider transitProvider;
    private final Clock scheduleClock;

    @Transactional(readOnly = true)
    public TodayCommute get(final double originLatitude, final double originLongitude) {
        final Optional<TransitOrigin> origin = origin(originLatitude, originLongitude);
        if (origin.isEmpty()) {
            log.warn("Commute unavailable because the origin coordinates are invalid");
            return TodayCommute.unavailable();
        }

        final CommutePreferences preferences = commutePreferencesService.get();
        if (!isVisible(preferences)) {
            return TodayCommute.hidden();
        }

        final Optional<ResolvedWorkday> workdayResult = upcomingWorkScheduleService.getTodayWorkday();
        if (workdayResult.isEmpty()) {
            log.warn("Commute unavailable because today's office destination could not be resolved");
            return TodayCommute.unavailable();
        }
        final ResolvedWorkday workday = workdayResult.orElseThrow();
        return route(origin.orElseThrow(), workday, preferences);
    }

    private Optional<TransitOrigin> origin(final double latitude, final double longitude) {
        try {
            return Optional.of(new TransitOrigin(latitude, longitude));
        } catch (IllegalArgumentException _) {
            return Optional.empty();
        }
    }

    private TodayCommute route(
            final TransitOrigin origin,
            final ResolvedWorkday workday,
            final CommutePreferences preferences) {
        if (workday.status() != ResolvedWorkday.Status.OFFICE) {
            return TodayCommute.hidden();
        }

        final LocalDate today = workday.date();
        final LocalDateTime targetArrival = today.atTime(workday.workingHoursStart())
                .minusMinutes(preferences.officeArrivalLeadMinutes());
        final ResolvedWorkday.OfficeLocation office = workday.office();
        if (office == null) {
            log.warn("Commute unavailable because today's office has no coordinates");
            return TodayCommute.unavailable();
        }
        final TransitRequest request = new TransitRequest(
                origin, office.latitude(), office.longitude(), today, targetArrival);
        final Optional<TransitJourney> journey = transitProvider.findJourney(request);
        if (journey.isEmpty()) {
            return TodayCommute.unavailable();
        }
        final TransitJourney candidate = journey.orElseThrow();
        if (candidate.arrival().isAfter(targetArrival)) {
            return TodayCommute.unavailable();
        }
        final Optional<PublicTransportSegment> firstTransit = candidate.firstPublicTransportSegment();
        if (firstTransit.isEmpty()) {
            return TodayCommute.unavailable();
        }
        return available(candidate, today, office.label(), firstTransit.orElseThrow(), preferences);
    }


    private boolean isVisible(final CommutePreferences preferences) {
        return LocalTime.now(scheduleClock).isBefore(preferences.visibilityCutoff());
    }

    private TodayCommute available(
            final TransitJourney journey,
            final LocalDate date,
            final String officeLabel,
            final PublicTransportSegment firstTransit,
            final CommutePreferences preferences) {
        final LocalDateTime leaveHome = firstTransit.departure()
                .minus(initialWalkingDuration(journey, firstTransit))
                .minusMinutes(preferences.transitAccessBufferMinutes());
        final LocalDateTime wakeUp = leaveHome.minusMinutes(preferences.wakeUpLeadMinutes());
        return new TodayCommute(
                TodayCommute.Status.AVAILABLE,
                date,
                floorToQuarterHour(wakeUp).toLocalTime(),
                leaveHome.toLocalTime(),
                journey.arrival().toLocalTime(),
                officeLabel,
                journey.totalDuration().toMinutes(),
                journey.transferCount(),
                journey.segments());
    }

    private Duration initialWalkingDuration(
            final TransitJourney journey, final PublicTransportSegment firstTransit) {
        final int firstTransitIndex = journey.segments().indexOf(firstTransit);
        return journey.segments().subList(0, firstTransitIndex).stream()
                .filter(WalkingSegment.class::isInstance)
                .map(WalkingSegment.class::cast)
                .map(segment -> Duration.between(segment.departure(), segment.arrival()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    private LocalDateTime floorToQuarterHour(final LocalDateTime value) {
        final int flooredMinute = value.getMinute() / 15 * 15;
        return value.withMinute(flooredMinute).withSecond(0).withNano(0);
    }
}
