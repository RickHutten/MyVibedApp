package nl.codestar.myvibedapp.schedule.application;

import java.util.List;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;

public interface ScheduleStore {

    WorkSchedule schedule();

    void saveSchedule(WorkSchedule schedule);

    List<SavedOffice> offices(boolean includeDeleted);

    SavedOffice saveOffice(SavedOffice office);

    List<RecurringRule> recurringRules();

    RecurringRule saveRecurringRule(RecurringRule rule);

    void deleteRecurringRule(UUID id);
}
