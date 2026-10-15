package nl.codestar.myvibedapp.commute.adapters.out.transitous;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import nl.codestar.myvibedapp.commute.application.TransitRequest;
import nl.codestar.myvibedapp.commute.domain.PublicTransportSegment;
import nl.codestar.myvibedapp.commute.domain.TransitJourney;
import nl.codestar.myvibedapp.commute.domain.TransitOrigin;
import nl.codestar.myvibedapp.commute.domain.WalkingSegment;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TransitousAdapterTest {

    private static final int HTTP_OK = 200;

    @Test
    void mapsTransitousV5JourneyWithEverySegmentAndUsesRealtimeTrackWhenAvailable() throws IOException {
        try (TransitousHttpStub stub = TransitousHttpStub.responding("""
                {
                  "itineraries": [{
                    "endTime": "2026-10-07T06:55:00Z",
                    "legs": [
                      {
                        "mode": "WALK",
                        "duration": 480,
                        "startTime": "2026-10-07T05:46:00Z",
                        "endTime": "2026-10-07T05:54:00Z",
                        "from": { "name": "Home" },
                        "to": { "name": "Amsterdam Zuid" }
                      },
                      {
                        "mode": "BUS",
                        "routeShortName": "34",
                        "headsign": "Central Station",
                        "startTime": "2026-10-07T05:54:00Z",
                        "endTime": "2026-10-07T06:45:00Z",
                        "from": {
                          "name": "Amsterdam Zuid",
                          "scheduledTrack": "3",
                          "track": "4"
                        },
                        "to": { "name": "Rokin" }
                      },
                      {
                        "mode": "WALK",
                        "startTime": "2026-10-07T06:45:00Z",
                        "endTime": "2026-10-07T06:50:00Z",
                        "from": { "name": "Rokin" },
                        "to": { "name": "Office" }
                      }
                    ]
                  }]
                }
                """)) {
            Optional<TransitJourney> result = adapterFor(stub).findJourney(request());

            assertThat(result).isPresent();
            TransitJourney journey = result.orElseThrow();
            assertThat(journey.arrival()).isEqualTo(LocalDateTime.of(2026, 10, 7, 8, 55));
            assertThat(journey.segments()).hasSize(3);
            assertThat(journey.segments().get(0)).isInstanceOf(WalkingSegment.class);
            assertThat(journey.segments().get(0).from()).contains("Home");
            assertThat(journey.segments().get(1)).isInstanceOf(PublicTransportSegment.class);
            PublicTransportSegment transit = (PublicTransportSegment) journey.segments().get(1);
            assertThat(transit.mode()).contains("BUS");
            assertThat(transit.service()).contains("34");
            assertThat(transit.from()).contains("Amsterdam Zuid");
            assertThat(transit.destination()).contains("Rokin");
            assertThat(transit.platform()).contains("4");
            assertThat(journey.segments().get(2)).isInstanceOf(WalkingSegment.class);
            assertThat(stub.requestUri().getQuery())
                    .contains("arriveBy=true")
                    .contains("time=2026-10-07T08:55:00+02:00");
            assertThat(stub.requestUri().getRawQuery())
                    .contains("time=2026-10-07T08:55:00%2B02:00");
        }
    }

    @Test
    void preservesPublicTransportSegmentsWhenOptionalProviderDetailsAreMissing() throws IOException {
        try (TransitousHttpStub stub = TransitousHttpStub.responding("""
                {
                  "itineraries": [{
                    "endTime": "2026-10-07T06:30:00Z",
                    "legs": [
                      {
                        "mode": "WALK",
                        "startTime": "2026-10-07T06:00:00Z",
                        "endTime": "2026-10-07T06:10:00Z"
                      },
                      {
                        "headsign": "Central Station",
                        "startTime": "2026-10-07T06:10:00Z",
                        "endTime": "2026-10-07T06:30:00Z"
                      }
                    ]
                  }]
                }
                """)) {
            Optional<TransitJourney> result = adapterFor(stub).findJourney(request());

            assertThat(result).isPresent();
            PublicTransportSegment transit = (PublicTransportSegment) result.orElseThrow().segments().get(1);
            assertThat(transit.mode()).isEmpty();
            assertThat(transit.service()).isEmpty();
            assertThat(transit.direction()).contains("Central Station");
            assertThat(transit.from()).isEmpty();
            assertThat(transit.destination()).isEmpty();
            assertThat(transit.platform()).isEmpty();
        }
    }

    @Test
    void readsItinerariesNestedUnderPlan() throws IOException {
        try (TransitousHttpStub stub = TransitousHttpStub.responding("""
                {
                  "plan": {
                    "itineraries": [{
                      "endTime": "2026-10-07T06:55:00Z",
                      "legs": [
                        {
                          "mode": "WALK",
                          "startTime": "2026-10-07T05:46:00Z",
                          "endTime": "2026-10-07T05:54:00Z"
                        },
                        {
                          "mode": "BUS",
                          "startTime": "2026-10-07T05:54:00Z",
                          "endTime": "2026-10-07T06:55:00Z"
                        }
                      ]
                    }]
                  }
                }
                """)) {
            assertThat(adapterFor(stub).findJourney(request())).isPresent();
        }
    }

    @Test
    void returnsEmptyWhenNoItineraryIsAvailable() throws IOException {
        try (TransitousHttpStub stub = TransitousHttpStub.responding("""
                {"itineraries": []}
                """)) {
            assertThat(adapterFor(stub).findJourney(request())).isEmpty();
        }
    }

    @Test
    void returnsEmptyWhenItineraryHasNoPublicTransportSegment() throws IOException {
        try (TransitousHttpStub stub = TransitousHttpStub.responding("""
                {
                  "itineraries": [{
                    "endTime": "2026-10-07T06:55:00Z",
                    "legs": [{
                      "mode": "WALK",
                      "startTime": "2026-10-07T05:46:00Z",
                      "endTime": "2026-10-07T06:55:00Z"
                    }]
                  }]
                }
                """)) {
            assertThat(adapterFor(stub).findJourney(request())).isEmpty();
        }
    }

    @Test
    void returnsEmptyWhenItineraryContainsAnInvalidTimestamp() throws IOException {
        try (TransitousHttpStub stub = TransitousHttpStub.responding("""
                {
                  "itineraries": [{
                    "endTime": "2026-10-07T06:55:00Z",
                    "legs": [{
                      "mode": "BUS",
                      "startTime": "not-a-timestamp",
                      "endTime": "2026-10-07T06:55:00Z"
                    }]
                  }]
                }
                """)) {
            assertThat(adapterFor(stub).findJourney(request())).isEmpty();
        }
    }

    @Test
    void returnsEmptyWhenTransitousReturnsAnHttpFailure() throws IOException {
        try (TransitousHttpStub stub = TransitousHttpStub.failing(503)) {
            assertThat(adapterFor(stub).findJourney(request())).isEmpty();
        }
    }

    private TransitousAdapter adapterFor(final TransitousHttpStub stub) {
        return new TransitousAdapter(
                new TransitousProperties(stub.baseUrl(), Duration.ofSeconds(2), Duration.ofSeconds(3)),
                "my-vibed-app");
    }

    private TransitRequest request() {
        return new TransitRequest(
                new TransitOrigin(52.3676, 4.9041),
                52.0907,
                5.1214,
                LocalDate.of(2026, 10, 7),
                LocalDateTime.of(2026, 10, 7, 8, 55));
    }

    private static final class TransitousHttpStub implements AutoCloseable {

        private final HttpServer server;
        private final byte[] responseBody;
        private final AtomicReference<Optional<URI>> requestUri = new AtomicReference<>(Optional.empty());

        private TransitousHttpStub(final String responseBody, final int status) throws IOException {
            this.responseBody = responseBody.getBytes(StandardCharsets.UTF_8);
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/api/v5/plan", exchange -> {
                requestUri.set(Optional.of(exchange.getRequestURI()));
                if (status == HTTP_OK) {
                    exchange.getResponseHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                    exchange.sendResponseHeaders(status, this.responseBody.length);
                    try (OutputStream output = exchange.getResponseBody()) {
                        output.write(this.responseBody);
                    }
                } else {
                    exchange.sendResponseHeaders(status, -1);
                    exchange.close();
                }
            });
            server.start();
        }

        static TransitousHttpStub responding(final String responseBody) throws IOException {
            return new TransitousHttpStub(responseBody, HTTP_OK);
        }

        static TransitousHttpStub failing(final int status) throws IOException {
            return new TransitousHttpStub("", status);
        }

        String baseUrl() {
            return "http://localhost:" + server.getAddress().getPort();
        }

        URI requestUri() {
            return requestUri.get().orElseThrow(() -> new AssertionError("The stub did not receive a request"));
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
