package nl.codestar.myvibedapp.schedule.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WorkScheduleTest {

    @Test
    @SuppressWarnings("java:S5778")
    void rejectsOfficeDayWithoutOffice() {
        assertThatThrownBy(() -> new WorkSchedule(
                new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                days(new ScheduleDay(ScheduleStatus.OFFICE, null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("office");
    }

    @Test
    void allowsWorkFromHomeAndNonWorkingDaysWithoutOffice() {
        assertThatCode(() -> new WorkSchedule(
                        new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                        days(new ScheduleDay(ScheduleStatus.WORK_FROM_HOME, null),
                                new ScheduleDay(ScheduleStatus.NON_WORKING, null))))
                .doesNotThrowAnyException();
    }

    @Test
    void allowsOfficeDayWithOfficeReference() {
        assertThatCode(() -> new WorkSchedule(
                        new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                        days(new ScheduleDay(ScheduleStatus.OFFICE, UUID.randomUUID()))))
                .doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("java:S5778")
    void rejectsInvalidWorkingHours() {
        assertThatThrownBy(() -> new WorkingHours(LocalTime.of(17, 0), LocalTime.of(9, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start");
    }

    private static Map<DayOfWeek, ScheduleDay> days(final ScheduleDay monday, final ScheduleDay saturday) {
        final Map<DayOfWeek, ScheduleDay> days = days(monday);
        days.put(DayOfWeek.SATURDAY, saturday);
        return days;
    }

    private static Map<DayOfWeek, ScheduleDay> days(final ScheduleDay monday) {
        final Map<DayOfWeek, ScheduleDay> days = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            days.put(day, new ScheduleDay(ScheduleStatus.NON_WORKING, null));
        }
        days.put(DayOfWeek.MONDAY, monday);
        return days;
    }
}
