package nl.codestar.myvibedapp.weather.adapters.in.web;

import nl.codestar.myvibedapp.weather.application.WeatherProvider;
import nl.codestar.myvibedapp.weather.application.WeatherUnavailableException;
import nl.codestar.myvibedapp.weather.domain.CurrentWeather;
import nl.codestar.myvibedapp.weather.domain.WeatherLocation;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WeatherControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ControllableWeatherProvider weatherProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
        weatherProvider.reset();
    }

    @Test
    void returnsCurrentWeatherForRequestedLocation() throws Exception {
        weatherProvider.returnWeather(new CurrentWeather(19.7, "Overcast", 85, 25.6, "W"));

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

        assertThat(weatherProvider.requestedLocation())
                .isEqualTo(new WeatherLocation(52.3676, 4.9041));
    }

    @Test
    void returnsServiceUnavailableWithoutProviderDetails() throws Exception {
        weatherProvider.failWith(new WeatherUnavailableException("Open-Meteo payload was invalid"));

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

    static final class ControllableWeatherProvider implements WeatherProvider {

        private @Nullable CurrentWeather weather;
        private @Nullable WeatherLocation requestedLocation;
        private @Nullable RuntimeException failure;

        @Override
        public CurrentWeather getCurrentWeather(WeatherLocation location) {
            requestedLocation = location;
            if (failure != null) {
                throw failure;
            }
            return Objects.requireNonNull(weather, "Test weather was not configured");
        }

        void returnWeather(CurrentWeather weather) {
            this.weather = weather;
        }

        void failWith(RuntimeException failure) {
            this.failure = failure;
        }

        WeatherLocation requestedLocation() {
            return Objects.requireNonNull(requestedLocation, "No weather request was made");
        }

        void reset() {
            weather = null;
            requestedLocation = null;
            failure = null;
        }
    }
}
