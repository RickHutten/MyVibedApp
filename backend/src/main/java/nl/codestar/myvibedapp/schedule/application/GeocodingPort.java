package nl.codestar.myvibedapp.schedule.application;

import java.util.List;

@FunctionalInterface
public interface GeocodingPort {

    List<AddressSuggestion> search(String query);

    record AddressSuggestion(
            String label,
            String address,
            double latitude,
            double longitude) {
    }
}
