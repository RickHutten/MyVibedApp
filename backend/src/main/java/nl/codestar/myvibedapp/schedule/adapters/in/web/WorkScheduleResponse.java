package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;

record WorkScheduleResponse(WorkingHoursResponse workingHours, List<DayResponse> days, List<OfficeResponse> offices) {

    static WorkScheduleResponse from(final WorkSchedule schedule, final List<SavedOffice> offices) {
        return new WorkScheduleResponse(
                new WorkingHoursResponse(schedule.workingHours().start().toString(), schedule.workingHours().end().toString()),
                Arrays.stream(DayOfWeek.values())
                        .map(day -> DayResponse.from(day, Objects.requireNonNull(schedule.days().get(day))))
                        .toList(),
                offices.stream().map(OfficeResponse::from).toList());
    }
}
