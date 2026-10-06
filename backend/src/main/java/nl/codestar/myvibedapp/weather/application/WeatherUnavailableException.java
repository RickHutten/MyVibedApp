package nl.codestar.myvibedapp.weather.application;

public final class WeatherUnavailableException extends RuntimeException {

    public WeatherUnavailableException(final String message) {
        super(message);
    }

    public WeatherUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
