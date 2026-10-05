package nl.codestar.myvibedapp.domain.weather;

public record CurrentWeather(
		double temperatureC,
		String condition,
		int precipitationProbabilityPercent,
		double windSpeedKmh,
		String windDirection) {
}
