package nl.codestar.myvibedapp.interfaces.weather.http;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import nl.codestar.myvibedapp.application.weather.WeatherService;
import nl.codestar.myvibedapp.domain.weather.CurrentWeather;
import nl.codestar.myvibedapp.domain.weather.WeatherLocation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
class WeatherController {

	private final WeatherService weatherService;

	WeatherController(WeatherService weatherService) {
		this.weatherService = weatherService;
	}

	@GetMapping("/current")
	CurrentWeatherResponse currentWeather(
			@RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
			@RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double longitude) {
		CurrentWeather weather = weatherService.getCurrentWeather(new WeatherLocation(latitude, longitude));
		return new CurrentWeatherResponse(
				weather.temperatureC(),
				weather.condition(),
				weather.precipitationProbabilityPercent(),
				weather.windSpeedKmh(),
				weather.windDirection());
	}
}
