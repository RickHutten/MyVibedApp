package nl.codestar.myvibedapp.weather.adapters.out.openmeteo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nl.codestar.myvibedapp.weather.application.WeatherProvider;
import nl.codestar.myvibedapp.weather.application.WeatherUnavailableException;
import nl.codestar.myvibedapp.weather.domain.CurrentWeather;
import nl.codestar.myvibedapp.weather.domain.WeatherLocation;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
final class OpenMeteoWeatherProvider implements WeatherProvider {

    @SuppressWarnings("java:S1192")
    private static final Map<Integer, String> CONDITIONS = Map.ofEntries(
            Map.entry(0, "Clear sky"),
            Map.entry(1, "Mainly clear"),
            Map.entry(2, "Partly cloudy"),
            Map.entry(3, "Overcast"),
            Map.entry(45, "Fog"),
            Map.entry(48, "Fog"),
            Map.entry(51, "Drizzle"),
            Map.entry(53, "Drizzle"),
            Map.entry(55, "Drizzle"),
            Map.entry(56, "Freezing drizzle"),
            Map.entry(57, "Freezing drizzle"),
            Map.entry(61, "Rain"),
            Map.entry(63, "Rain"),
            Map.entry(65, "Rain"),
            Map.entry(66, "Freezing rain"),
            Map.entry(67, "Freezing rain"),
            Map.entry(71, "Snowfall"),
            Map.entry(73, "Snowfall"),
            Map.entry(75, "Snowfall"),
            Map.entry(77, "Snow grains"),
            Map.entry(80, "Rain showers"),
            Map.entry(81, "Rain showers"),
            Map.entry(82, "Rain showers"),
            Map.entry(85, "Snow showers"),
            Map.entry(86, "Snow showers"),
            Map.entry(95, "Thunderstorm"),
            Map.entry(96, "Thunderstorm with hail"),
            Map.entry(99, "Thunderstorm with hail"));

    private final RestClient restClient;
    private final Validator validator;

    OpenMeteoWeatherProvider(final OpenMeteoProperties properties, final Validator validator) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
        this.validator = validator;
    }

    @Override
    public CurrentWeather getCurrentWeather(final WeatherLocation location) {
        try {
            OpenMeteoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", location.latitude())
                            .queryParam("longitude", location.longitude())
                            .queryParam("current", "temperature_2m,weather_code,wind_speed_10m,wind_direction_10m")
                            .queryParam("hourly", "precipitation_probability")
                            .queryParam("temperature_unit", "celsius")
                            .queryParam("wind_speed_unit", "kmh")
                            .queryParam("timezone", "auto")
                            .queryParam("forecast_days", 1)
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponse.class);
            if (response == null) {
                throw new WeatherUnavailableException("Open-Meteo response is incomplete");
            }
            return map(response);
        } catch (RestClientException | DateTimeException | IllegalArgumentException exception) {
            throw new WeatherUnavailableException("Unable to retrieve current weather", exception);
        }
    }

    private CurrentWeather map(final OpenMeteoResponse response) {
        validateResponse(response);
        final Current current = response.current();
        final Hourly hourly = response.hourly();
        final int hourIndex = currentHourIndex(current, hourly);
        validateCurrentValues(current);
        return new CurrentWeather(
                current.temperatureC(),
                conditionFor(current.weatherCode()),
                hourly.precipitationProbability().get(hourIndex),
                current.windSpeedKmh(),
                windDirectionFor(current.windDirectionDegrees()));
    }

    private void validateResponse(final OpenMeteoResponse response) {
        Set<ConstraintViolation<OpenMeteoResponse>> violations = validator.validate(response);
        if (!violations.isEmpty()) {
            throw new WeatherUnavailableException("Open-Meteo response is incomplete");
        }
    }

    private int currentHourIndex(final Current current, final Hourly hourly) {
        final String currentHour = LocalDateTime.parse(current.time()).truncatedTo(ChronoUnit.HOURS).toString();
        final int hourIndex = hourly.time().indexOf(currentHour);
        if (hourIndex < 0
                || hourIndex >= hourly.precipitationProbability().size()) {
            throw new WeatherUnavailableException("Open-Meteo response has no current-hour rain probability");
        }
        return hourIndex;
    }

    private void validateCurrentValues(final Current current) {
        if (!Double.isFinite(current.temperatureC())
                || !Double.isFinite(current.windSpeedKmh())
                || current.windSpeedKmh() < 0
                || !Double.isFinite(current.windDirectionDegrees())
                || current.windDirectionDegrees() < 0
                || current.windDirectionDegrees() > 360) {
            throw new WeatherUnavailableException("Open-Meteo response contains invalid weather values");
        }
    }

    private String conditionFor(final int code) {
        return Optional.ofNullable(CONDITIONS.get(code))
                .orElseThrow(() -> new WeatherUnavailableException("Open-Meteo response contains an unknown weather code"));
    }

    private String windDirectionFor(final double degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.floor((degrees + 22.5) % 360 / 45);
        return directions[index];
    }

    private record OpenMeteoResponse(@NotNull @Valid Current current, @NotNull @Valid Hourly hourly) {
    }

    private record Current(
            @NotBlank String time,
            @NotNull @JsonProperty("temperature_2m") Double temperatureC,
            @NotNull @JsonProperty("weather_code") Integer weatherCode,
            @NotNull @JsonProperty("wind_speed_10m") Double windSpeedKmh,
            @NotNull @JsonProperty("wind_direction_10m") Double windDirectionDegrees) {
    }

    private record Hourly(
            @NotEmpty List<@NotBlank String> time,
            @NotEmpty @JsonProperty("precipitation_probability") List<@NotNull @Min(0) @Max(100) Integer> precipitationProbability) {
    }
}
