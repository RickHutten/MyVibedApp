package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.time.DayOfWeek;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.domain.ScheduleDay;
import org.jspecify.annotations.Nullable;

record DayResponse(String day, String status, @Nullable UUID officeId) {

    static DayResponse from(final DayOfWeek day, final ScheduleDay scheduleDay) {
        return new DayResponse(day.name(), scheduleDay.status().name(), scheduleDay.officeId());
    }
}
