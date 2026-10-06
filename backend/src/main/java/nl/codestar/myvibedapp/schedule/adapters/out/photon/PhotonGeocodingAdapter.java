package nl.codestar.myvibedapp.schedule.adapters.out.photon;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import nl.codestar.myvibedapp.schedule.application.GeocodingPort;
import nl.codestar.myvibedapp.schedule.application.GeocodingUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

@Component
final class PhotonGeocodingAdapter implements GeocodingPort {

    private final RestClient restClient;

    PhotonGeocodingAdapter(
            @Value("${schedule.photon.base-url:https://photon.komoot.io}") final String baseUrl,
            @Value("${schedule.photon.connect-timeout:2s}") final Duration connectTimeout,
            @Value("${schedule.photon.read-timeout:3s}") final Duration readTimeout,
            @Value("${spring.application.name:my-vibed-app}") final String userAgent) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    @Override
    public List<AddressSuggestion> search(final String query) {
        try {
            final JsonNode response = Objects.requireNonNull(restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api")
                            .queryParam("q", query)
                            .queryParam("limit", 5)
                            .build())
                    .retrieve()
                    .body(JsonNode.class));
            final JsonNode features = response.path("features");
            final List<AddressSuggestion> suggestions = new ArrayList<>();
            if (!features.isArray()) {
                return List.of();
            }
            features.forEach(feature -> toSuggestion(feature).ifPresent(suggestions::add));
            return List.copyOf(suggestions);
        } catch (RestClientException exception) {
            throw new GeocodingUnavailableException(exception);
        }
    }

    @SuppressWarnings("java:S5850")
    private Optional<AddressSuggestion> toSuggestion(final JsonNode feature) {
        final JsonNode coordinates = feature.path("geometry").path("coordinates");
        if (!coordinates.isArray() || coordinates.size() < 2
                || !coordinates.get(0).isNumber() || !coordinates.get(1).isNumber()) {
            return Optional.empty();
        }
        final JsonNode properties = feature.path("properties");
        final String label = text(properties, "name", "Address");
        final String address = String.join(", ",
                text(properties, "housenumber", ""),
                text(properties, "street", ""),
                text(properties, "city", ""),
                text(properties, "country", ""))
                .replaceAll("(?:, )+", ", ")
                .replaceAll("^, |, $", "");
        return Optional.of(new AddressSuggestion(
                label,
                address.isBlank() ? label : address,
                coordinates.get(1).doubleValue(),
                coordinates.get(0).doubleValue()));
    }

    private String text(final JsonNode node, final String field, final String fallback) {
        final JsonNode value = node.get(field);
        return value == null || value.isNull() ? fallback : value.asString();
    }
}
