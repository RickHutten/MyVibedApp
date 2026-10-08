package nl.codestar.myvibedapp.commute.application;

import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.commute.domain.CommutePreferences;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommutePreferencesService {

    private final CommutePreferencesStore store;

    @Transactional(readOnly = true)
    public CommutePreferences get() {
        return store.preferences();
    }

    @Transactional
    public CommutePreferences replace(final CommutePreferences preferences) {
        store.save(preferences);
        return preferences;
    }
}
