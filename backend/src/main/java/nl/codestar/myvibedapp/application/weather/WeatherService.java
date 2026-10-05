package nl.codestar.myvibedapp.application.weather;

import nl.codestar.myvibedapp.domain.weather.CurrentWeather;
import nl.codestar.myvibedapp.domain.weather.WeatherLocation;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

	private final WeatherProvider weatherProvider;

	public WeatherService(WeatherProvider weatherProvider) {
		this.weatherProvider = weatherProvider;
	}

	public CurrentWeather getCurrentWeather(WeatherLocation location) {
		return weatherProvider.getCurrentWeather(location);
	}
}
