package nl.codestar.myvibedapp.schedule.adapters.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

record WorkScheduleRequest(
        @Valid @NotNull WorkingHoursRequest workingHours,
        @NotNull List<@Valid DayRequest> days) {
}
