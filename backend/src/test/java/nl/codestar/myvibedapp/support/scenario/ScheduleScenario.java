package nl.codestar.myvibedapp.support.scenario;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.application.ScheduleStore;
import nl.codestar.myvibedapp.schedule.domain.OneOffOverride;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.ScheduleDay;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;
import nl.codestar.myvibedapp.schedule.domain.WorkingHours;
import org.jspecify.annotations.Nullable;

public record ScheduleScenario(
        WorkSchedule schedule,
        List<SavedOffice> offices,
        List<RecurringRule> recurringRules,
        List<OneOffOverride> oneOffOverrides) {

    public ScheduleScenario {
        offices = List.copyOf(offices);
        recurringRules = List.copyOf(recurringRules);
        oneOffOverrides = List.copyOf(oneOffOverrides);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void persist(final ScheduleStore scheduleStore) {
        offices.forEach(scheduleStore::saveOffice);
        scheduleStore.saveSchedule(schedule);
        recurringRules.forEach(scheduleStore::saveRecurringRule);
        oneOffOverrides.forEach(scheduleStore::saveOneOffOverride);
    }

    public static final class Builder {

        private final Map<DayOfWeek, ScheduleDay> days = defaultDays();
        private final List<SavedOffice> offices = new ArrayList<>();
        private final List<RecurringRule> recurringRules = new ArrayList<>();
        private final List<OneOffOverride> oneOffOverrides = new ArrayList<>();
        private final WorkingHours workingHours = new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0));

        public Builder weeklyDay(
                final DayOfWeek day, final ScheduleStatus status, @Nullable final UUID officeId) {
            days.put(day, new ScheduleDay(status, officeId));
            return this;
        }

        public Builder office(final UUID id, final String label) {
            return office(new SavedOffice(id, label, label + " address", 52.37, 4.90, false));
        }

        public Builder deletedOffice(final UUID id, final String label) {
            return office(new SavedOffice(id, label, label + " address", 52.37, 4.90, true));
        }

        public Builder office(final SavedOffice office) {
            offices.add(office);
            return this;
        }

        public Builder recurringRule(final RecurringRule rule) {
            recurringRules.add(rule);
            return this;
        }

        public Builder oneOffOverride(final OneOffOverride override) {
            oneOffOverrides.add(override);
            return this;
        }

        public ScheduleScenario build() {
            return new ScheduleScenario(
                    new WorkSchedule(workingHours, days), offices, recurringRules, oneOffOverrides);
        }

        private static Map<DayOfWeek, ScheduleDay> defaultDays() {
            final Map<DayOfWeek, ScheduleDay> days = new EnumMap<>(DayOfWeek.class);
            for (final DayOfWeek day : DayOfWeek.values()) {
                days.put(day, new ScheduleDay(ScheduleStatus.NON_WORKING, null));
            }
            return days;
        }
    }
}
