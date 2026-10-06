package nl.codestar.myvibedapp.weather.adapters.in.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.weather.application.WeatherService;
import nl.codestar.myvibedapp.weather.domain.CurrentWeather;
import nl.codestar.myvibedapp.weather.domain.WeatherLocation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    CurrentWeatherResponse currentWeather(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") final double latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") final double longitude) {
        final CurrentWeather weather = weatherService.getCurrentWeather(new WeatherLocation(latitude, longitude));
        return CurrentWeatherResponse.from(weather);
    }
}
