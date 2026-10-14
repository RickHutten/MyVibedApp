package nl.codestar.myvibedapp.schedule.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.schedule.domain.ResolvedScheduleDay;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.ScheduleResolver;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpcomingWorkScheduleService {

    private final ScheduleStore scheduleStore;
    private final Clock scheduleClock;

    @Transactional(readOnly = true)
    public UpcomingWorkSchedule getUpcomingWorkSchedule() {
        final LocalDate today = LocalDate.now(scheduleClock);
        final var schedule = scheduleStore.schedule();
        final var recurringRules = scheduleStore.recurringRules();
        final var overrides = scheduleStore.oneOffOverrides();
        final Map<UUID, String> officeLabels = scheduleStore.offices(true).stream()
                .collect(Collectors.toUnmodifiableMap(SavedOffice::id, SavedOffice::label));

        final ResolvedScheduleDay resolvedToday = ScheduleResolver.resolve(
                today, schedule, recurringRules, overrides);
        final Optional<ResolvedScheduleDay> resolvedNext = ScheduleResolver.findNextWorkingDay(
                today, schedule, recurringRules, overrides);
        return new UpcomingWorkSchedule(
                toWorkDay(resolvedToday, officeLabels),
                resolvedNext.map(day -> toWorkDay(day, officeLabels)));
    }

    @Transactional(readOnly = true)
    public Optional<ResolvedWorkday> getTodayWorkday() {
        final LocalDate today = LocalDate.now(scheduleClock);
        final var schedule = scheduleStore.schedule();
        final ResolvedScheduleDay resolvedToday = ScheduleResolver.resolve(
                today, schedule, scheduleStore.recurringRules(), scheduleStore.oneOffOverrides());
        final ResolvedWorkday.Status status = toWorkdayStatus(resolvedToday.status());
        final Optional<ResolvedWorkday.OfficeLocation> office = resolvedToday.status() == ScheduleStatus.OFFICE
                ? officeLocation(resolvedToday)
                : Optional.empty();
        return office.map(location -> new ResolvedWorkday(
                        today, status, location, schedule.workingHours().start()))
                .or(() -> resolvedToday.status() == ScheduleStatus.OFFICE
                        ? Optional.empty()
                        : Optional.of(new ResolvedWorkday(
                                today, status, null, schedule.workingHours().start())));
    }

    private Optional<ResolvedWorkday.OfficeLocation> officeLocation(final ResolvedScheduleDay day) {
        final UUID officeId = Objects.requireNonNull(day.officeId(), "An office day requires an office");
        return scheduleStore.offices(true).stream()
                .filter(candidate -> candidate.id().equals(officeId))
                .findFirst()
                .map(office -> new ResolvedWorkday.OfficeLocation(office.label(), office.latitude(), office.longitude()));
    }

    private ResolvedWorkday.Status toWorkdayStatus(final ScheduleStatus status) {
        return switch (status) {
            case OFFICE -> ResolvedWorkday.Status.OFFICE;
            case WORK_FROM_HOME -> ResolvedWorkday.Status.WORK_FROM_HOME;
            case NON_WORKING -> ResolvedWorkday.Status.NON_WORKING;
        };
    }

    private UpcomingWorkDay toWorkDay(
            final ResolvedScheduleDay day, final Map<UUID, String> officeLabels) {
        final String officeLabel = day.status() == ScheduleStatus.OFFICE
                ? officeLabels.get(Objects.requireNonNull(day.officeId(), "An office day requires an office"))
                : null;
        if (day.status() == ScheduleStatus.OFFICE && officeLabel == null) {
            throw new IllegalStateException("Resolved office was not found");
        }
        return new UpcomingWorkDay(day.date(), day.status(), officeLabel);
    }
}
