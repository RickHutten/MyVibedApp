package nl.codestar.myvibedapp.interfaces.weather.http;

import nl.codestar.myvibedapp.application.weather.WeatherProvider;
import nl.codestar.myvibedapp.application.weather.WeatherUnavailableException;
import nl.codestar.myvibedapp.domain.weather.CurrentWeather;
import nl.codestar.myvibedapp.domain.weather.WeatherLocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WeatherControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ControllableWeatherProvider weatherProvider;

	@Test
	void returnsCurrentWeatherForRequestedLocation() throws Exception {
		weatherProvider.failure = null;
		weatherProvider.weather = new CurrentWeather(19.7, "Overcast", 85, 25.6, "W");

		mockMvc.perform(get("/api/weather/current")
					.param("latitude", "52.3676")
					.param("longitude", "4.9041"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.temperatureC").value(19.7))
				.andExpect(jsonPath("$.condition").value("Overcast"))
				.andExpect(jsonPath("$.precipitationProbabilityPercent").value(85))
				.andExpect(jsonPath("$.windSpeedKmh").value(25.6))
				.andExpect(jsonPath("$.windDirection").value("W"));

		assertThat(weatherProvider.requestedLocation)
				.isEqualTo(new WeatherLocation(52.3676, 4.9041));
	}

	@Test
	void returnsServiceUnavailableWithoutProviderDetails() throws Exception {
		weatherProvider.failure = new WeatherUnavailableException("Open-Meteo payload was invalid");

		mockMvc.perform(get("/api/weather/current")
					.param("latitude", "52.3676")
					.param("longitude", "4.9041"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.detail").value("Current weather is unavailable"))
				.andExpect(content().string(org.hamcrest.Matchers.not(
						org.hamcrest.Matchers.containsString("Open-Meteo"))));
	}

	@Test
	void rejectsCoordinatesOutsideTheirValidRanges() throws Exception {
		mockMvc.perform(get("/api/weather/current")
					.param("latitude", "91")
					.param("longitude", "181"))
				.andExpect(status().isBadRequest());
	}

	@TestConfiguration
	static class WeatherTestConfiguration {

		@Bean
		@Primary
		ControllableWeatherProvider controllableWeatherProvider() {
			return new ControllableWeatherProvider();
		}
	}

	static class ControllableWeatherProvider implements WeatherProvider {
		private CurrentWeather weather;
		private WeatherLocation requestedLocation;
		private RuntimeException failure;

		@Override
		public CurrentWeather getCurrentWeather(WeatherLocation location) {
			if (failure != null) {
				throw failure;
			}
			requestedLocation = location;
			return weather;
		}
	}
}
