package nl.codestar.myvibedapp.weather.application;

import nl.codestar.myvibedapp.weather.domain.CurrentWeather;
import nl.codestar.myvibedapp.weather.domain.WeatherLocation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WeatherService {

    private final WeatherProvider weatherProvider;

    public CurrentWeather getCurrentWeather(WeatherLocation location) {
        return weatherProvider.getCurrentWeather(location);
    }
}
