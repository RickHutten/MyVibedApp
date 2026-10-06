package nl.codestar.myvibedapp.schedule.application;

public final class GeocodingUnavailableException extends RuntimeException {

    public GeocodingUnavailableException(final Throwable cause) {
        super("Address search is unavailable", cause);
    }
}
