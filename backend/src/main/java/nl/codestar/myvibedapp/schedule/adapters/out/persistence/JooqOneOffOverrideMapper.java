package nl.codestar.myvibedapp.schedule.adapters.out.persistence;

import static nl.codestar.myvibedapp.jooq.Tables.ONE_OFF_SCHEDULE_OVERRIDES;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.codestar.myvibedapp.schedule.domain.OneOffOverride;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import org.jooq.Record;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class JooqOneOffOverrideMapper {

    static OneOffOverride from(final Record row) {
        return new OneOffOverride(
                row.get(ONE_OFF_SCHEDULE_OVERRIDES.ID),
                row.get(ONE_OFF_SCHEDULE_OVERRIDES.START_DATE),
                row.get(ONE_OFF_SCHEDULE_OVERRIDES.END_DATE),
                ScheduleStatus.valueOf(row.get(ONE_OFF_SCHEDULE_OVERRIDES.STATUS)),
                row.get(ONE_OFF_SCHEDULE_OVERRIDES.OFFICE_ID));
    }
}
