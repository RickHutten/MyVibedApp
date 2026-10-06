package nl.codestar.myvibedapp.weather.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import nl.codestar.myvibedapp.weather.application.WeatherProvider;
import nl.codestar.myvibedapp.weather.application.WeatherUnavailableException;
import nl.codestar.myvibedapp.weather.domain.CurrentWeather;
import nl.codestar.myvibedapp.weather.domain.WeatherLocation;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("local")
@Testcontainers
class WeatherControllerTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.6");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ControllableWeatherProvider weatherProvider;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configurePostgres(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
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
                .andExpect(content().string(Matchers.not(
                        Matchers.containsString("Open-Meteo"))));
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

        private Optional<CurrentWeather> weather = Optional.empty();
        private Optional<WeatherLocation> requestedLocation = Optional.empty();
        private Optional<RuntimeException> failure = Optional.empty();

        @Override
        public CurrentWeather getCurrentWeather(final WeatherLocation location) {
            requestedLocation = Optional.of(location);
            failure.ifPresent(configuredFailure -> {
                throw configuredFailure;
            });
            return weather.orElseThrow(() -> new AssertionError("Test weather was not configured"));
        }

        void returnWeather(final CurrentWeather weather) {
            this.weather = Optional.of(weather);
        }

        void failWith(final RuntimeException failure) {
            this.failure = Optional.of(failure);
        }

        WeatherLocation requestedLocation() {
            return requestedLocation.orElseThrow(() -> new AssertionError("No weather request was made"));
        }

        void reset() {
            weather = Optional.empty();
            requestedLocation = Optional.empty();
            failure = Optional.empty();
        }
    }
}
