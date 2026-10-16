package nl.codestar.myvibedapp.commute.adapters.out.transitous;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import nl.codestar.myvibedapp.commute.application.TransitProvider;
import nl.codestar.myvibedapp.commute.application.TransitRequest;
import nl.codestar.myvibedapp.commute.domain.JourneySegment;
import nl.codestar.myvibedapp.commute.domain.PublicTransportSegment;
import nl.codestar.myvibedapp.commute.domain.TransitJourney;
import nl.codestar.myvibedapp.commute.domain.WalkingSegment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

@Component
@Slf4j
final class TransitousAdapter implements TransitProvider {

    private static final ZoneId SCHEDULE_ZONE = ZoneId.of("Europe/Amsterdam");
    private static final String WALK_MODE = "WALK";

    private final RestClient restClient;

    TransitousAdapter(
            final TransitousProperties properties,
            @Value("${spring.application.name:my-vibed-app}") final String userAgent) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();
    }

    @Override
    public Optional<TransitJourney> findJourney(final TransitRequest request) {
        try {
            final String encodedRequestUri = UriComponentsBuilder.fromPath("/api/v5/plan")
                    .queryParam("fromPlace", "%s,%s".formatted(
                            request.origin().latitude(), request.origin().longitude()))
                    .queryParam("toPlace", "%s,%s".formatted(
                            request.destinationLatitude(), request.destinationLongitude()))
                    .queryParam("date", request.date())
                    .queryParam(
                            "time",
                            request.targetArrival()
                                    .atZone(SCHEDULE_ZONE)
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                    .queryParam("arriveBy", true)
                    .queryParam("numItineraries", 5)
                    .build()
                    .encode()
                    .toUriString()
                    .replace("+", "%2B");
            final URI requestUri = URI.create(encodedRequestUri);
            final JsonNode response = restClient.get()
                    .uri(requestUri)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                log.warn(
                        "Transitous returned an empty response for date {} and target arrival {}",
                        request.date(),
                        request.targetArrival());
                return Optional.empty();
            }
            return journeyFrom(response, request);
        } catch (RestClientException exception) {
            log.warn(
                    "Transitous request failed for date {} and target arrival {}",
                    request.date(),
                    request.targetArrival(),
                    exception);
            return Optional.empty();
        }
    }

    private Optional<TransitJourney> journeyFrom(final JsonNode response, final TransitRequest request) {
        String itinerariesPropertyName = "itineraries";
        final JsonNode itineraries = response.path(itinerariesPropertyName).isArray()
                ? response.path(itinerariesPropertyName)
                : response.path("plan").path(itinerariesPropertyName);
        if (!itineraries.isArray()) {
            log.warn(
                    "Transitous response is missing itineraries for date {} and target arrival {}",
                    request.date(),
                    request.targetArrival());
            return Optional.empty();
        }
        if (itineraries.isEmpty()) {
            log.debug(
                    "Transitous returned no itineraries for date {} and target arrival {}",
                    request.date(),
                    request.targetArrival());
            return Optional.empty();
        }
        final Optional<TransitJourney> journey = StreamSupport.stream(itineraries.spliterator(), false)
                .map(itinerary -> parseItinerarySafely(itinerary, request))
                .flatMap(Optional::stream)
                .filter(candidate -> !candidate.arrival().isAfter(request.targetArrival()))
                .max(Comparator.comparing(TransitJourney::arrival));
        if (journey.isEmpty()) {
            log.debug(
                    "Transitous returned no itinerary arriving by date {} and target arrival {}",
                    request.date(),
                    request.targetArrival());
        }
        return journey;
    }

    private Optional<TransitJourney> parseItinerarySafely(
            final JsonNode itinerary, final TransitRequest request) {
        try {
            return parseItinerary(itinerary);
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Transitous returned an itinerary that could not be mapped for date {} and target arrival {}",
                    request.date(),
                    request.targetArrival(),
                    exception);
            return Optional.empty();
        }
    }

    private Optional<TransitJourney> parseItinerary(final JsonNode itinerary) {
        final JsonNode rawLegs = itinerary.path("legs");
        final Optional<ParsedSegments> parsedLegs = parseLegs(rawLegs);
        if (parsedLegs.isEmpty()) {
            return Optional.empty();
        }
        final Optional<LocalDateTime> arrival = timestamp(itinerary.path("endTime"));
        if (arrival.isEmpty()) {
            return Optional.empty();
        }
        final ParsedSegments parsed = parsedLegs.orElseThrow();
        if (parsed.segments().stream().noneMatch(PublicTransportSegment.class::isInstance)) {
            return Optional.empty();
        }
        return Optional.of(new TransitJourney(
                parsed.segments().getFirst().departure(), arrival.orElseThrow(), parsed.segments()));
    }

    private Optional<ParsedSegments> parseLegs(final JsonNode rawLegs) {
        if (!rawLegs.isArray()) {
            return Optional.empty();
        }
        final List<JourneySegment> segments = new ArrayList<>();
        int transferNumber = 0;
        for (final JsonNode rawLeg : rawLegs) {
            final Optional<JourneySegment> segment = parseSegment(rawLeg, transferNumber);
            if (segment.isEmpty()) {
                return Optional.empty();
            }
            final JourneySegment value = segment.orElseThrow();
            segments.add(value);
            if (value instanceof PublicTransportSegment) {
                transferNumber++;
            }
        }
        if (segments.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ParsedSegments(segments));
    }

    private Optional<JourneySegment> parseSegment(final JsonNode rawLeg, final int transferNumber) {
        final Optional<LocalDateTime> departure = timestamp(rawLeg.path("startTime"));
        final Optional<LocalDateTime> arrival = timestamp(rawLeg.path("endTime"));
        if (departure.isEmpty() || arrival.isEmpty()) {
            return Optional.empty();
        }
        final Optional<String> from = location(rawLeg, "from");
        final Optional<String> to = location(rawLeg, "to");
        if (WALK_MODE.equalsIgnoreCase(rawLeg.path("mode").asString(""))) {
            return Optional.of(new WalkingSegment(departure.orElseThrow(), arrival.orElseThrow(), from, to));
        }
        return Optional.of(new PublicTransportSegment(
                transferNumber,
                firstTextOptional(rawLeg, "mode"),
                firstTextOptional(rawLeg, "routeShortName", "routeLongName", "route.shortName", "route.longName"),
                firstTextOptional(rawLeg, "headsign", "route.headsign"),
                departure.orElseThrow(),
                arrival.orElseThrow(),
                from,
                to,
                platform(rawLeg)));
    }

    private Optional<LocalDateTime> timestamp(final JsonNode value) {
        if (value.isNumber()) {
            return Optional.of(LocalDateTime.ofInstant(Instant.ofEpochMilli(value.asLong()), SCHEDULE_ZONE));
        }
        if (value.isString()) {
            try {
                return Optional.of(LocalDateTime.ofInstant(Instant.parse(value.asString()), SCHEDULE_ZONE));
            } catch (DateTimeParseException _) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<String> platform(final JsonNode leg) {
        final JsonNode stop = leg.path("from");
        return firstTextOptional(stop, "track", "scheduledTrack", "stop.platformCode", "stop.platformName", "stop.platform");
    }

    private Optional<String> location(final JsonNode leg, final String key) {
        return firstTextOptional(leg, key + ".name", key + ".stop.name", key + ".stopName", key + ".label");
    }

    private Optional<String> firstTextOptional(final JsonNode node, final String... paths) {
        for (final String path : paths) {
            JsonNode value = node;
            final StringTokenizer segments = new StringTokenizer(path, ".");
            while (segments.hasMoreTokens()) {
                final String segment = segments.nextToken();
                value = value.path(segment);
            }
            if (value.isString() && !value.asString().isBlank()) {
                return Optional.of(value.asString());
            }
        }
        return Optional.empty();
    }

    private record ParsedSegments(List<JourneySegment> segments) {}
}
