package nl.codestar.myvibedapp.schedule.adapters.in.web;

import nl.codestar.myvibedapp.schedule.application.UpcomingWorkSchedule;
import org.jspecify.annotations.Nullable;

record UpcomingWorkScheduleResponse(
        UpcomingWorkDayResponse today, @Nullable UpcomingWorkDayResponse nextWorkingDay) {

    static UpcomingWorkScheduleResponse from(final UpcomingWorkSchedule schedule) {
        return new UpcomingWorkScheduleResponse(
                UpcomingWorkDayResponse.from(schedule.today()),
                schedule.nextWorkingDay().map(UpcomingWorkDayResponse::from).orElse(null));
    }
}
