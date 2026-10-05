package nl.codestar.myvibedapp.application.weather;

import nl.codestar.myvibedapp.domain.weather.CurrentWeather;
import nl.codestar.myvibedapp.domain.weather.WeatherLocation;

public interface WeatherProvider {

	CurrentWeather getCurrentWeather(WeatherLocation location);
}
