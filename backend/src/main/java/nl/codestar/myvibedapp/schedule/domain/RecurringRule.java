package nl.codestar.myvibedapp.schedule.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record RecurringRule(
        UUID id,
        RecurrenceLevel level,
        int interval,
        Set<DayOfWeek> weekdays,
        @Nullable MonthlyPattern monthlyPattern,
        LocalDate startDate,
        @Nullable LocalDate endDate,
        ScheduleStatus status,
        @Nullable UUID officeId) {

    private static final int MIN_INTERVAL = 1;

    public RecurringRule {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(weekdays, "weekdays");
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(status, "status");
        if (interval < MIN_INTERVAL) {
            throw new IllegalArgumentException("Rule interval must be positive");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Rule end date cannot be before its start date");
        }
        if (level == RecurrenceLevel.WEEKS && weekdays.isEmpty()) {
            throw new IllegalArgumentException("Weekly rules require at least one weekday");
        }
        if (level == RecurrenceLevel.MONTHS && monthlyPattern == null) {
            throw new IllegalArgumentException("Monthly rules require a monthly pattern");
        }
        if (level != RecurrenceLevel.MONTHS && monthlyPattern != null) {
            throw new IllegalArgumentException("Only monthly rules can have a monthly pattern");
        }
        if (level != RecurrenceLevel.WEEKS && !weekdays.isEmpty()) {
            throw new IllegalArgumentException("Only weekly rules can select weekdays");
        }
        if (status == ScheduleStatus.OFFICE && officeId == null) {
            throw new IllegalArgumentException("An office outcome requires an office");
        }
        if (status != ScheduleStatus.OFFICE && officeId != null) {
            throw new IllegalArgumentException("Only office outcomes can reference an office");
        }
        weekdays = Set.copyOf(weekdays);
    }

}
