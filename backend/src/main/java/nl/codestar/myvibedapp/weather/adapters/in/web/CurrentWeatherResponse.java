package nl.codestar.myvibedapp.weather.adapters.in.web;

import nl.codestar.myvibedapp.weather.domain.CurrentWeather;

record CurrentWeatherResponse(
        double temperatureC,
        String condition,
        int precipitationProbabilityPercent,
        double windSpeedKmh,
        String windDirection) {

    static CurrentWeatherResponse from(CurrentWeather weather) {
        return new CurrentWeatherResponse(
                weather.temperatureC(),
                weather.condition(),
                weather.precipitationProbabilityPercent(),
                weather.windSpeedKmh(),
                weather.windDirection());
    }
}
