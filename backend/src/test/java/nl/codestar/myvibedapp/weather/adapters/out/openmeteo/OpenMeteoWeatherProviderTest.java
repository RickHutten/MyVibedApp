package nl.codestar.myvibedapp.weather.adapters.out.openmeteo;

import com.sun.net.httpserver.HttpServer;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import nl.codestar.myvibedapp.weather.application.WeatherUnavailableException;
import nl.codestar.myvibedapp.weather.domain.CurrentWeather;
import nl.codestar.myvibedapp.weather.domain.WeatherLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenMeteoWeatherProviderTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void mapsCurrentConditionsAndCurrentHourRainProbability() throws IOException {
        try (OpenMeteoHttpStub stub = OpenMeteoHttpStub.responding(validPayload())) {
            OpenMeteoWeatherProvider provider = providerFor(stub);

            CurrentWeather weather = provider.getCurrentWeather(new WeatherLocation(52.3676, 4.9041));

            assertThat(weather).isEqualTo(new CurrentWeather(19.7, "Overcast", 85, 25.6, "W"));
            assertThat(stub.requestUri().getPath()).isEqualTo("/v1/forecast");
            assertThat(stub.requestUri().getQuery())
                    .contains("latitude=52.3676")
                    .contains("longitude=4.9041")
                    .contains("temperature_unit=celsius")
                    .contains("wind_speed_unit=kmh")
                    .contains("timezone=auto")
                    .contains("forecast_days=1");
        }
    }

    @Test
    void rejectsIncompleteProviderResponses() throws IOException {
        try (OpenMeteoHttpStub stub = OpenMeteoHttpStub.responding("""
                {
                  "current": {
                    "time": "2026-09-04T15:45",
                    "temperature_2m": 19.7,
                    "weather_code": 3,
                    "wind_speed_10m": 25.6,
                    "wind_direction_10m": 267
                  },
                  "hourly": {}
                }
                """)) {
            OpenMeteoWeatherProvider provider = providerFor(stub);

            WeatherLocation location = new WeatherLocation(52.3676, 4.9041);
            assertThatThrownBy(() -> provider.getCurrentWeather(location))
                    .isInstanceOf(WeatherUnavailableException.class);
        }
    }

    @Test
    void translatesMalformedProviderTimestamps() throws IOException {
        try (OpenMeteoHttpStub stub = OpenMeteoHttpStub.responding(payloadWithValues("not-a-timestamp", 3, 19.7, 25.6, 267, 85))) {
            OpenMeteoWeatherProvider provider = providerFor(stub);

            WeatherLocation location = new WeatherLocation(52.3676, 4.9041);
            assertThatThrownBy(() -> provider.getCurrentWeather(location))
                    .isInstanceOf(WeatherUnavailableException.class);
        }
    }

    @Test
    void translatesProviderHttpFailures() throws IOException {
        try (OpenMeteoHttpStub stub = OpenMeteoHttpStub.failing(503)) {
            OpenMeteoWeatherProvider provider = providerFor(stub);

            WeatherLocation location = new WeatherLocation(52.3676, 4.9041);
            assertThatThrownBy(() -> provider.getCurrentWeather(location))
                    .isInstanceOf(WeatherUnavailableException.class);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "999, 18.4, 248, 65",
            "3, -1, 248, 65",
            "3, 18.4, -1, 65",
            "3, 18.4, 248, -1",
            "3, 18.4, 248, 101"
    })
    void rejectsProviderValuesOutsideTheirValidRanges(
            int weatherCode,
            double windSpeed,
            double windDirection,
            int precipitationProbability) throws IOException {
        try (OpenMeteoHttpStub stub = OpenMeteoHttpStub.responding(
                payloadWithValues("2026-09-04T15:45", weatherCode, 19.7, windSpeed, windDirection,
                        precipitationProbability))) {
            OpenMeteoWeatherProvider provider = providerFor(stub);

            WeatherLocation location = new WeatherLocation(52.3676, 4.9041);
            assertThatThrownBy(() -> provider.getCurrentWeather(location))
                    .isInstanceOf(WeatherUnavailableException.class);
        }
    }

    private OpenMeteoWeatherProvider providerFor(OpenMeteoHttpStub stub) {
        return new OpenMeteoWeatherProvider(RestClient.builder()
                .baseUrl(stub.baseUrl())
                .build(), VALIDATOR);
    }

    private String validPayload() {
        return payloadWithValues("2026-09-04T15:45", 3, 19.7, 25.6, 267, 85);
    }

    private String payloadWithValues(
            String time,
            int weatherCode,
            double temperature,
            double windSpeed,
            double windDirection,
            int precipitationProbability) {
        return """
                {
                  "current": {
                    "time": "%s",
                    "temperature_2m": %s,
                    "weather_code": %d,
                    "wind_speed_10m": %s,
                    "wind_direction_10m": %s
                  },
                  "hourly": {
                    "time": ["2026-09-04T15:00"],
                    "precipitation_probability": [%d]
                  }
                }
                """.formatted(time, temperature, weatherCode, windSpeed, windDirection,
                precipitationProbability);
    }

    private static final class OpenMeteoHttpStub implements AutoCloseable {

        private final HttpServer server;
        private final byte[] responseBody;
        private volatile @Nullable URI requestUri;

        private OpenMeteoHttpStub(String responseBody, int status) throws IOException {
            this.responseBody = responseBody.getBytes(StandardCharsets.UTF_8);
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/v1/forecast", exchange -> {
                requestUri = exchange.getRequestURI();
                if (status == 200) {
                    exchange.getResponseHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                    exchange.sendResponseHeaders(status, responseBody.length());
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

        static OpenMeteoHttpStub responding(String responseBody) throws IOException {
            return new OpenMeteoHttpStub(responseBody, 200);
        }

        static OpenMeteoHttpStub failing(int status) throws IOException {
            return new OpenMeteoHttpStub("", status);
        }

        String baseUrl() {
            return "http://localhost:" + server.getAddress().getPort();
        }

        URI requestUri() {
            return Objects.requireNonNull(requestUri, "The stub did not receive a request");
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
