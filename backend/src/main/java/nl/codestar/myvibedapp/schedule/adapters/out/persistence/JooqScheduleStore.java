package nl.codestar.myvibedapp.schedule.adapters.out.persistence;

import static nl.codestar.myvibedapp.jooq.Tables.RECURRING_SCHEDULE_RULES;
import static nl.codestar.myvibedapp.jooq.Tables.SAVED_OFFICES;
import static nl.codestar.myvibedapp.jooq.Tables.WORK_SCHEDULE;
import static nl.codestar.myvibedapp.jooq.Tables.WORK_SCHEDULE_DAYS;

import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.jooq.tables.records.WorkScheduleDaysRecord;
import nl.codestar.myvibedapp.schedule.application.ScheduleStore;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.ScheduleDay;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;
import nl.codestar.myvibedapp.schedule.domain.WorkingHours;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JooqScheduleStore implements ScheduleStore {

    private final DSLContext dsl;

    @Override
    public WorkSchedule schedule() {
        final WorkingHours hours = dsl.select(WORK_SCHEDULE.START_TIME, WORK_SCHEDULE.END_TIME)
                .from(WORK_SCHEDULE)
                .where(WORK_SCHEDULE.ID.isTrue())
                .fetchSingle(row -> new WorkingHours(row.get(WORK_SCHEDULE.START_TIME), row.get(WORK_SCHEDULE.END_TIME)));
        final Map<DayOfWeek, ScheduleDay> days = new EnumMap<>(DayOfWeek.class);
        dsl.select(WORK_SCHEDULE_DAYS.DAY_OF_WEEK, WORK_SCHEDULE_DAYS.STATUS, WORK_SCHEDULE_DAYS.OFFICE_ID)
                .from(WORK_SCHEDULE_DAYS)
                .orderBy(WORK_SCHEDULE_DAYS.DAY_OF_WEEK)
                .fetch()
                .forEach(row -> {
            final DayOfWeek day = DayOfWeek.of(row.get(WORK_SCHEDULE_DAYS.DAY_OF_WEEK));
            final ScheduleStatus status = ScheduleStatus.valueOf(row.get(WORK_SCHEDULE_DAYS.STATUS));
            final UUID officeId = row.get(WORK_SCHEDULE_DAYS.OFFICE_ID);
            days.put(day, new ScheduleDay(status, officeId));
        });
        return new WorkSchedule(hours, days);
    }

    @Override
    public void saveSchedule(final WorkSchedule schedule) {
        dsl.update(WORK_SCHEDULE)
                .set(WORK_SCHEDULE.START_TIME, schedule.workingHours().start())
                .set(WORK_SCHEDULE.END_TIME, schedule.workingHours().end())
                .where(WORK_SCHEDULE.ID.isTrue())
                .execute();
        dsl.batchUpdate(schedule.days().entrySet().stream()
                .map(entry -> {
                    final WorkScheduleDaysRecord daysRecord = new WorkScheduleDaysRecord();
                    daysRecord.setDayOfWeek((short) entry.getKey().getValue());
                    daysRecord.setStatus(entry.getValue().status().name());
                    final UUID officeId = entry.getValue().officeId();
                    if (officeId != null) {
                        daysRecord.setOfficeId(officeId);
                    }
                    return daysRecord;
                })
                .toList()).execute();
    }

    @Override
    public List<SavedOffice> offices(final boolean includeDeleted) {
        return dsl.select(SAVED_OFFICES.ID, SAVED_OFFICES.LABEL, SAVED_OFFICES.ADDRESS,
                        SAVED_OFFICES.LATITUDE, SAVED_OFFICES.LONGITUDE, SAVED_OFFICES.DELETED)
                .from(SAVED_OFFICES)
                .where(includeDeleted ? DSL.noCondition() : SAVED_OFFICES.DELETED.isFalse())
                .orderBy(DSL.lower(SAVED_OFFICES.LABEL))
                .fetch(row -> new SavedOffice(
                        row.get(SAVED_OFFICES.ID), row.get(SAVED_OFFICES.LABEL), row.get(SAVED_OFFICES.ADDRESS),
                        row.get(SAVED_OFFICES.LATITUDE), row.get(SAVED_OFFICES.LONGITUDE), row.get(SAVED_OFFICES.DELETED)));
    }

    @Override
    public SavedOffice saveOffice(final SavedOffice office) {
        dsl.insertInto(SAVED_OFFICES)
                .set(SAVED_OFFICES.ID, office.id())
                .set(SAVED_OFFICES.LABEL, office.label())
                .set(SAVED_OFFICES.ADDRESS, office.address())
                .set(SAVED_OFFICES.LATITUDE, office.latitude())
                .set(SAVED_OFFICES.LONGITUDE, office.longitude())
                .set(SAVED_OFFICES.DELETED, office.deleted())
                .onConflict(SAVED_OFFICES.ID)
                .doUpdate()
                .set(SAVED_OFFICES.LABEL, office.label())
                .set(SAVED_OFFICES.ADDRESS, office.address())
                .set(SAVED_OFFICES.LATITUDE, office.latitude())
                .set(SAVED_OFFICES.LONGITUDE, office.longitude())
                .set(SAVED_OFFICES.DELETED, office.deleted())
                .execute();
        return office;
    }

    @Override
    public List<RecurringRule> recurringRules() {
        return dsl.selectFrom(RECURRING_SCHEDULE_RULES)
                .orderBy(RECURRING_SCHEDULE_RULES.START_DATE, RECURRING_SCHEDULE_RULES.ID)
                .fetch(JooqRecurringRuleMapper::from);
    }

    @Override
    public RecurringRule saveRecurringRule(final RecurringRule rule) {
        dsl.insertInto(RECURRING_SCHEDULE_RULES)
                .set(RECURRING_SCHEDULE_RULES.ID, rule.id())
                .set(RECURRING_SCHEDULE_RULES.RECURRENCE_LEVEL, rule.level().name())
                .set(RECURRING_SCHEDULE_RULES.INTERVAL_VALUE, rule.interval())
                .set(RECURRING_SCHEDULE_RULES.WEEKDAY_MASK, JooqRecurringRuleMapper.weekdayMask(rule))
                .set(RECURRING_SCHEDULE_RULES.MONTHLY_PATTERN_TYPE, JooqRecurringRuleMapper.monthlyPatternType(rule))
                .set(RECURRING_SCHEDULE_RULES.MONTHLY_CALENDAR_DAY, JooqRecurringRuleMapper.calendarDay(rule))
                .set(RECURRING_SCHEDULE_RULES.MONTHLY_WEEKDAY, JooqRecurringRuleMapper.monthlyWeekday(rule))
                .set(RECURRING_SCHEDULE_RULES.MONTHLY_OCCURRENCE, JooqRecurringRuleMapper.monthlyOccurrence(rule))
                .set(RECURRING_SCHEDULE_RULES.START_DATE, rule.startDate())
                .set(RECURRING_SCHEDULE_RULES.END_DATE, rule.endDate())
                .set(RECURRING_SCHEDULE_RULES.STATUS, rule.status().name())
                .set(RECURRING_SCHEDULE_RULES.OFFICE_ID, rule.officeId())
                .onConflict(RECURRING_SCHEDULE_RULES.ID)
                .doUpdate()
                .set(RECURRING_SCHEDULE_RULES.RECURRENCE_LEVEL, rule.level().name())
                .set(RECURRING_SCHEDULE_RULES.INTERVAL_VALUE, rule.interval())
                .set(RECURRING_SCHEDULE_RULES.WEEKDAY_MASK, JooqRecurringRuleMapper.weekdayMask(rule))
                .set(RECURRING_SCHEDULE_RULES.MONTHLY_PATTERN_TYPE, JooqRecurringRuleMapper.monthlyPatternType(rule))
                .set(RECURRING_SCHEDULE_RULES.MONTHLY_CALENDAR_DAY, JooqRecurringRuleMapper.calendarDay(rule))
                .set(RECURRING_SCHEDULE_RULES.MONTHLY_WEEKDAY, JooqRecurringRuleMapper.monthlyWeekday(rule))
                .set(RECURRING_SCHEDULE_RULES.MONTHLY_OCCURRENCE, JooqRecurringRuleMapper.monthlyOccurrence(rule))
                .set(RECURRING_SCHEDULE_RULES.START_DATE, rule.startDate())
                .set(RECURRING_SCHEDULE_RULES.END_DATE, rule.endDate())
                .set(RECURRING_SCHEDULE_RULES.STATUS, rule.status().name())
                .set(RECURRING_SCHEDULE_RULES.OFFICE_ID, rule.officeId())
                .execute();
        return rule;
    }

    @Override
    public void deleteRecurringRule(final UUID id) {
        dsl.deleteFrom(RECURRING_SCHEDULE_RULES)
                .where(RECURRING_SCHEDULE_RULES.ID.eq(id))
                .execute();
    }
}
