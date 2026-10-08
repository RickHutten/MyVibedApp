package nl.codestar.myvibedapp.commute.adapters.in.web;

import nl.codestar.myvibedapp.commute.domain.CommutePreferences;

record CommutePreferencesResponse(
        long wakeUpLeadMinutes,
        long transitAccessBufferMinutes,
        long officeArrivalLeadMinutes) {

    static CommutePreferencesResponse from(final CommutePreferences preferences) {
        return new CommutePreferencesResponse(
                preferences.wakeUpLeadMinutes(),
                preferences.transitAccessBufferMinutes(),
                preferences.officeArrivalLeadMinutes());
    }
}
