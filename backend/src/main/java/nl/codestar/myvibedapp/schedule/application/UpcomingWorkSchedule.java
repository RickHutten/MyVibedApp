package nl.codestar.myvibedapp.schedule.application;

import java.util.Optional;

public record UpcomingWorkSchedule(UpcomingWorkDay today, Optional<UpcomingWorkDay> nextWorkingDay) {
}
