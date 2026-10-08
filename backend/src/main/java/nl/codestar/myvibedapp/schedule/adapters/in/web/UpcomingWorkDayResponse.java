package nl.codestar.myvibedapp.schedule.adapters.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import nl.codestar.myvibedapp.schedule.application.UpcomingWorkDay;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
record UpcomingWorkDayResponse(LocalDate date, ScheduleStatus status, @Nullable String officeLabel) {

    static UpcomingWorkDayResponse from(final UpcomingWorkDay day) {
        return new UpcomingWorkDayResponse(day.date(), day.status(), day.officeLabel());
    }
}
