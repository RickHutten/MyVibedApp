package nl.codestar.myvibedapp.schedule.domain;

import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record WorkSchedule(WorkingHours workingHours, Map<DayOfWeek, ScheduleDay> days) {

    public WorkSchedule {
        Objects.requireNonNull(workingHours, "workingHours");
        Objects.requireNonNull(days, "days");
        if (!days.keySet().equals(Map.of(
                DayOfWeek.MONDAY, true,
                DayOfWeek.TUESDAY, true,
                DayOfWeek.WEDNESDAY, true,
                DayOfWeek.THURSDAY, true,
                DayOfWeek.FRIDAY, true,
                DayOfWeek.SATURDAY, true,
                DayOfWeek.SUNDAY, true).keySet())) {
            throw new IllegalArgumentException("A weekly schedule must contain all seven days");
        }
        days.forEach((day, scheduleDay) -> {
            Objects.requireNonNull(day, "day");
            Objects.requireNonNull(scheduleDay, "scheduleDay");
            if (scheduleDay.status() == ScheduleStatus.OFFICE && scheduleDay.officeId() == null) {
                throw new IllegalArgumentException("An office day requires an office");
            }
            if (scheduleDay.status() != ScheduleStatus.OFFICE && scheduleDay.officeId() != null) {
                throw new IllegalArgumentException("Only office days can reference an office");
            }
        });
        days = Map.copyOf(new EnumMap<>(days));
    }
}
