package nl.codestar.myvibedapp.interfaces.weather.openmeteo;

import nl.codestar.myvibedapp.application.weather.WeatherUnavailableException;
import nl.codestar.myvibedapp.domain.weather.CurrentWeather;
import nl.codestar.myvibedapp.domain.weather.WeatherLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenMeteoWeatherProviderTest {

	@Test
	void mapsCurrentConditionsAndCurrentHourRainProbability() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://weather.test");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider(builder.build());

		server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://weather.test/v1/forecast")))
				.andExpect(method(HttpMethod.GET))
				.andExpect(queryParam("latitude", "52.3676"))
				.andExpect(queryParam("longitude", "4.9041"))
				.andExpect(queryParam("temperature_unit", "celsius"))
				.andExpect(queryParam("wind_speed_unit", "kmh"))
				.andExpect(queryParam("timezone", "auto"))
				.andExpect(queryParam("forecast_days", "1"))
				.andRespond(withSuccess("""
						{
						  "current": {
						    "time": "2026-09-04T15:45",
						    "temperature_2m": 19.7,
						    "weather_code": 3,
						    "wind_speed_10m": 25.6,
						    "wind_direction_10m": 267
						  },
						  "hourly": {
						    "time": ["2026-09-04T14:00", "2026-09-04T15:00", "2026-09-04T16:00"],
						    "precipitation_probability": [100, 85, 60]
						  }
						}
						""", MediaType.APPLICATION_JSON));

		CurrentWeather weather = provider.getCurrentWeather(new WeatherLocation(52.3676, 4.9041));

		assertThat(weather).isEqualTo(new CurrentWeather(19.7, "Overcast", 85, 25.6, "W"));
		server.verify();
	}

	@Test
	void rejectsIncompleteProviderResponses() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://weather.test");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider(builder.build());

		server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://weather.test/v1/forecast")))
				.andRespond(withSuccess("""
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
						""", MediaType.APPLICATION_JSON));

		WeatherLocation location = new WeatherLocation(52.3676, 4.9041);
		assertThatThrownBy(() -> provider.getCurrentWeather(location))
				.isInstanceOf(WeatherUnavailableException.class);
		server.verify();
	}

	@Test
	void translatesMalformedProviderTimestamps() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://weather.test");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider(builder.build());

		server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://weather.test/v1/forecast")))
				.andRespond(withSuccess("""
						{
						  "current": {
						    "time": "not-a-timestamp",
						    "temperature_2m": 19.7,
						    "weather_code": 3,
						    "wind_speed_10m": 25.6,
						    "wind_direction_10m": 267
						  },
						  "hourly": {
						    "time": ["2026-09-04T15:00"],
						    "precipitation_probability": [85]
						  }
						}
						""", MediaType.APPLICATION_JSON));

		WeatherLocation location = new WeatherLocation(52.3676, 4.9041);
		assertThatThrownBy(() -> provider.getCurrentWeather(location))
				.isInstanceOf(WeatherUnavailableException.class);
		server.verify();
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
			int precipitationProbability) {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://weather.test");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider(builder.build());

		server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://weather.test/v1/forecast")))
				.andRespond(withSuccess("""
						{
						  "current": {
						    "time": "2026-09-04T15:45",
						    "temperature_2m": 19.7,
						    "weather_code": %d,
						    "wind_speed_10m": %s,
						    "wind_direction_10m": %s
						  },
						  "hourly": {
						    "time": ["2026-09-04T15:00"],
						    "precipitation_probability": [%d]
						  }
						}
						""".formatted(weatherCode, windSpeed, windDirection, precipitationProbability), MediaType.APPLICATION_JSON));

		WeatherLocation location = new WeatherLocation(52.3676, 4.9041);
		assertThatThrownBy(() -> provider.getCurrentWeather(location))
				.isInstanceOf(WeatherUnavailableException.class);
		server.verify();
	}
}
