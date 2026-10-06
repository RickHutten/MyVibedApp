package nl.codestar.myvibedapp.weather.application;

import nl.codestar.myvibedapp.weather.domain.CurrentWeather;
import nl.codestar.myvibedapp.weather.domain.WeatherLocation;

@FunctionalInterface
public interface WeatherProvider {

    CurrentWeather getCurrentWeather(WeatherLocation location);
}
