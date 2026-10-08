package nl.codestar.myvibedapp.schedule.application;

import java.time.LocalDate;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import org.jspecify.annotations.Nullable;

public record UpcomingWorkDay(LocalDate date, ScheduleStatus status, @Nullable String officeLabel) {

    public UpcomingWorkDay {
        if (status == ScheduleStatus.OFFICE && officeLabel == null) {
            throw new IllegalArgumentException("An office day requires an office label");
        }
        if (status != ScheduleStatus.OFFICE && officeLabel != null) {
            throw new IllegalArgumentException("Only office days can have an office label");
        }
    }
}
