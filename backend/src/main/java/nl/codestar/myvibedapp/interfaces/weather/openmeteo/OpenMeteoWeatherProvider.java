package nl.codestar.myvibedapp.interfaces.weather.openmeteo;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.codestar.myvibedapp.application.weather.WeatherProvider;
import nl.codestar.myvibedapp.application.weather.WeatherUnavailableException;
import nl.codestar.myvibedapp.domain.weather.CurrentWeather;
import nl.codestar.myvibedapp.domain.weather.WeatherLocation;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

final class OpenMeteoWeatherProvider implements WeatherProvider {

	private final RestClient restClient;

	OpenMeteoWeatherProvider(RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public CurrentWeather getCurrentWeather(WeatherLocation location) {
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
			return map(response);
		} catch (RestClientException | DateTimeException | IllegalArgumentException exception) {
			throw new WeatherUnavailableException("Unable to retrieve current weather", exception);
		}
	}

	private CurrentWeather map(OpenMeteoResponse response) {
		if (response == null || response.current() == null || response.hourly() == null) {
			throw new WeatherUnavailableException("Open-Meteo response is incomplete");
		}
		Current current = response.current();
		Hourly hourly = response.hourly();
		if (current.time() == null
				|| current.temperatureC() == null
				|| current.weatherCode() == null
				|| current.windSpeedKmh() == null
				|| current.windDirectionDegrees() == null
				|| hourly.time() == null
				|| hourly.precipitationProbability() == null) {
			throw new WeatherUnavailableException("Open-Meteo response is incomplete");
		}
		String currentHour = LocalDateTime.parse(current.time()).truncatedTo(ChronoUnit.HOURS).toString();
		int hourIndex = hourly.time().indexOf(currentHour);
		if (hourIndex < 0
				|| hourIndex >= hourly.precipitationProbability().size()
				|| hourly.precipitationProbability().get(hourIndex) == null) {
			throw new WeatherUnavailableException("Open-Meteo response has no current-hour rain probability");
		}
		int precipitationProbability = hourly.precipitationProbability().get(hourIndex);
		if (!Double.isFinite(current.temperatureC())
				|| !Double.isFinite(current.windSpeedKmh())
				|| current.windSpeedKmh() < 0
				|| !Double.isFinite(current.windDirectionDegrees())
				|| current.windDirectionDegrees() < 0
				|| current.windDirectionDegrees() > 360
				|| precipitationProbability < 0
				|| precipitationProbability > 100) {
			throw new WeatherUnavailableException("Open-Meteo response contains invalid weather values");
		}
		return new CurrentWeather(
				current.temperatureC(),
				conditionFor(current.weatherCode()),
				precipitationProbability,
				current.windSpeedKmh(),
				windDirectionFor(current.windDirectionDegrees()));
	}

	private String conditionFor(int code) {
		return switch (code) {
			case 0 -> "Clear sky";
			case 1 -> "Mainly clear";
			case 2 -> "Partly cloudy";
			case 3 -> "Overcast";
			case 45, 48 -> "Fog";
			case 51, 53, 55 -> "Drizzle";
			case 56, 57 -> "Freezing drizzle";
			case 61, 63, 65 -> "Rain";
			case 66, 67 -> "Freezing rain";
			case 71, 73, 75 -> "Snowfall";
			case 77 -> "Snow grains";
			case 80, 81, 82 -> "Rain showers";
			case 85, 86 -> "Snow showers";
			case 95 -> "Thunderstorm";
			case 96, 99 -> "Thunderstorm with hail";
			default -> throw new WeatherUnavailableException("Open-Meteo response contains an unknown weather code");
		};
	}

	private String windDirectionFor(double degrees) {
		String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
		int index = (int) Math.floor((degrees + 22.5) % 360 / 45);
		return directions[index];
	}

	private record OpenMeteoResponse(Current current, Hourly hourly) {
	}

	private record Current(
			String time,
			@JsonProperty("temperature_2m") Double temperatureC,
			@JsonProperty("weather_code") Integer weatherCode,
			@JsonProperty("wind_speed_10m") Double windSpeedKmh,
			@JsonProperty("wind_direction_10m") Double windDirectionDegrees) {
	}

	private record Hourly(
			List<String> time,
			@JsonProperty("precipitation_probability") List<Integer> precipitationProbability) {
	}
}
