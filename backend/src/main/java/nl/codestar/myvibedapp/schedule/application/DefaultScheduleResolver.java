package nl.codestar.myvibedapp.schedule.application;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.schedule.domain.ScheduleDay;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class DefaultScheduleResolver implements ScheduleResolver {

    private final ScheduleStore scheduleStore;

    @Override
    @Transactional(readOnly = true)
    public ScheduleDay resolve(final LocalDate date) {
        return ScheduleResolver.resolve(scheduleStore.schedule(), scheduleStore.recurringRules(), date);
    }
}
