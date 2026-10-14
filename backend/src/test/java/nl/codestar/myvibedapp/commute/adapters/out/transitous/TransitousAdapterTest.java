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
            TransitousAdapter adapter = new TransitousAdapter(
                    new TransitousProperties(stub.baseUrl(), Duration.ofSeconds(2), Duration.ofSeconds(3)),
                    "my-vibed-app");

            Optional<TransitJourney> result = adapter.findJourney(new TransitRequest(
                    new TransitOrigin(52.3676, 4.9041),
                    52.0907,
                    5.1214,
                    LocalDate.of(2026, 10, 7),
                    LocalDateTime.of(2026, 10, 7, 8, 55)));

            assertThat(result).isPresent();
            TransitJourney journey = result.orElseThrow();
            assertThat(journey.arrival()).isEqualTo(LocalDateTime.of(2026, 10, 7, 8, 55));
            assertThat(journey.segments()).hasSize(3);
            assertThat(journey.segments().get(0)).isInstanceOf(WalkingSegment.class);
            assertThat(((WalkingSegment) journey.segments().get(0)).from()).contains("Home");
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
            TransitousAdapter adapter = new TransitousAdapter(
                    new TransitousProperties(stub.baseUrl(), Duration.ofSeconds(2), Duration.ofSeconds(3)),
                    "my-vibed-app");

            Optional<TransitJourney> result = adapter.findJourney(new TransitRequest(
                    new TransitOrigin(52.3676, 4.9041),
                    52.0907,
                    5.1214,
                    LocalDate.of(2026, 10, 7),
                    LocalDateTime.of(2026, 10, 7, 8, 55)));

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

    private static final class TransitousHttpStub implements AutoCloseable {

        private final HttpServer server;
        private final byte[] responseBody;
        private final AtomicReference<Optional<URI>> requestUri = new AtomicReference<>(Optional.empty());

        private TransitousHttpStub(final String responseBody) throws IOException {
            this.responseBody = responseBody.getBytes(StandardCharsets.UTF_8);
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/api/v5/plan", exchange -> {
                requestUri.set(Optional.of(exchange.getRequestURI()));
                exchange.getResponseHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                exchange.sendResponseHeaders(200, this.responseBody.length);
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(this.responseBody);
                }
            });
            server.start();
        }

        static TransitousHttpStub responding(final String responseBody) throws IOException {
            return new TransitousHttpStub(responseBody);
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
