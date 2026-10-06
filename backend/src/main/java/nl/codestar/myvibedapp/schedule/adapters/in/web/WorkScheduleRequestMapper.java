package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.codestar.myvibedapp.schedule.domain.ScheduleDay;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;
import nl.codestar.myvibedapp.schedule.domain.WorkingHours;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class WorkScheduleRequestMapper {

    static WorkSchedule toDomain(final WorkScheduleRequest request) {
        final Map<DayOfWeek, ScheduleDay> days = new EnumMap<>(DayOfWeek.class);
        request.days().forEach(dayRequest -> {
            final DayOfWeek day = parseDay(dayRequest.day());
            if (days.put(day, new ScheduleDay(parseStatus(dayRequest.status()), dayRequest.officeId())) != null) {
                throw new IllegalArgumentException("Each day may only be configured once");
            }
        });
        return new WorkSchedule(
                new WorkingHours(parseTime(request.workingHours().start()), parseTime(request.workingHours().end())),
                days);
    }

    private static DayOfWeek parseDay(final String value) {
        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown day: " + value, exception);
        }
    }

    private static ScheduleStatus parseStatus(final String value) {
        try {
            return ScheduleStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown schedule status: " + value, exception);
        }
    }

    private static LocalTime parseTime(final String value) {
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Working hours must use HH:mm values", exception);
        }
    }
}
