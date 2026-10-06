package nl.codestar.myvibedapp.weather.adapters.in.web;

import nl.codestar.myvibedapp.weather.application.WeatherUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
final class WeatherExceptionHandler {

    @ExceptionHandler(WeatherUnavailableException.class)
    ProblemDetail handleWeatherUnavailable() {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setDetail("Current weather is unavailable");
        return problem;
    }
}
