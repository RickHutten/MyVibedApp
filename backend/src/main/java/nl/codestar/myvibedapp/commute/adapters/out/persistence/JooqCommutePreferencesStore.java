package nl.codestar.myvibedapp.commute.adapters.out.persistence;

import static nl.codestar.myvibedapp.jooq.Tables.COMMUTE_PREFERENCES;

import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.commute.application.CommutePreferencesStore;
import nl.codestar.myvibedapp.commute.domain.CommutePreferences;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JooqCommutePreferencesStore implements CommutePreferencesStore {

    private final DSLContext dsl;

    @Override
    public CommutePreferences preferences() {
        return dsl.select(
                        COMMUTE_PREFERENCES.WAKE_UP_LEAD_MINUTES,
                        COMMUTE_PREFERENCES.TRANSIT_ACCESS_BUFFER_MINUTES,
                        COMMUTE_PREFERENCES.OFFICE_ARRIVAL_LEAD_MINUTES)
                .from(COMMUTE_PREFERENCES)
                .where(COMMUTE_PREFERENCES.ID.isTrue())
                .fetchOptional()
                .map(row -> new CommutePreferences(
                        row.get(COMMUTE_PREFERENCES.WAKE_UP_LEAD_MINUTES),
                        row.get(COMMUTE_PREFERENCES.TRANSIT_ACCESS_BUFFER_MINUTES),
                        row.get(COMMUTE_PREFERENCES.OFFICE_ARRIVAL_LEAD_MINUTES)))
                .orElseGet(CommutePreferences::defaults);
    }

    @Override
    public void save(final CommutePreferences preferences) {
        dsl.insertInto(COMMUTE_PREFERENCES)
                .set(COMMUTE_PREFERENCES.ID, true)
                .set(COMMUTE_PREFERENCES.WAKE_UP_LEAD_MINUTES, preferences.wakeUpLeadMinutes())
                .set(COMMUTE_PREFERENCES.TRANSIT_ACCESS_BUFFER_MINUTES, preferences.transitAccessBufferMinutes())
                .set(COMMUTE_PREFERENCES.OFFICE_ARRIVAL_LEAD_MINUTES, preferences.officeArrivalLeadMinutes())
                .onConflict(COMMUTE_PREFERENCES.ID)
                .doUpdate()
                .set(COMMUTE_PREFERENCES.WAKE_UP_LEAD_MINUTES, preferences.wakeUpLeadMinutes())
                .set(COMMUTE_PREFERENCES.TRANSIT_ACCESS_BUFFER_MINUTES, preferences.transitAccessBufferMinutes())
                .set(COMMUTE_PREFERENCES.OFFICE_ARRIVAL_LEAD_MINUTES, preferences.officeArrivalLeadMinutes())
                .execute();
    }
}
