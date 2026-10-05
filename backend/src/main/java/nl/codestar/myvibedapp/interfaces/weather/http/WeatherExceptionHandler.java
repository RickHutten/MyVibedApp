package nl.codestar.myvibedapp.interfaces.weather.http;

import nl.codestar.myvibedapp.application.weather.WeatherUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class WeatherExceptionHandler {

	@ExceptionHandler(WeatherUnavailableException.class)
	ProblemDetail handleWeatherUnavailable() {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
		problem.setDetail("Current weather is unavailable");
		return problem;
	}
}
