package nl.codestar.myvibedapp.schedule.application;

import java.util.List;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;

public interface ScheduleStore {

    WorkSchedule schedule();

    void saveSchedule(WorkSchedule schedule);

    List<SavedOffice> offices(boolean includeDeleted);

    SavedOffice saveOffice(SavedOffice office);
}
