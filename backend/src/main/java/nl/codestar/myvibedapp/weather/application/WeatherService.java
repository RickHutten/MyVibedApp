package nl.codestar.myvibedapp.weather.application;

import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.weather.domain.CurrentWeather;
import nl.codestar.myvibedapp.weather.domain.WeatherLocation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class WeatherService {

    private final WeatherProvider weatherProvider;

    public CurrentWeather getCurrentWeather(final WeatherLocation location) {
        return weatherProvider.getCurrentWeather(location);
    }
}
