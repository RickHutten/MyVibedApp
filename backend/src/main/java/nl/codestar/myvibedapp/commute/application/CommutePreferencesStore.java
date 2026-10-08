package nl.codestar.myvibedapp.commute.application;

import nl.codestar.myvibedapp.commute.domain.CommutePreferences;

public interface CommutePreferencesStore {

    CommutePreferences preferences();

    void save(CommutePreferences preferences);
}
