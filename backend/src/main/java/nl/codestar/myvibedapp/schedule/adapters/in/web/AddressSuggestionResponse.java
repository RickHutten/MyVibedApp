package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.util.List;
import nl.codestar.myvibedapp.schedule.application.GeocodingPort.AddressSuggestion;

record AddressSuggestionResponse(String label, String address, double latitude, double longitude) {

    static AddressSuggestionResponse from(final AddressSuggestion suggestion) {
        return new AddressSuggestionResponse(suggestion.label(), suggestion.address(), suggestion.latitude(), suggestion.longitude());
    }

    static List<AddressSuggestionResponse> from(final List<AddressSuggestion> suggestions) {
        return suggestions.stream().map(AddressSuggestionResponse::from).toList();
    }
}
