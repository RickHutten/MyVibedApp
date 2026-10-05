package nl.codestar.myvibedapp.interfaces.weather.http;

record CurrentWeatherResponse(
		double temperatureC,
		String condition,
		int precipitationProbabilityPercent,
		double windSpeedKmh,
		String windDirection) {
}
