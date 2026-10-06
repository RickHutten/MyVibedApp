package nl.codestar.myvibedapp.weather.domain;

public record CurrentWeather(
        double temperatureC,
        String condition,
        int precipitationProbabilityPercent,
        double windSpeedKmh,
        String windDirection) {
}
