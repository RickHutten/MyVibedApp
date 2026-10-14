package nl.codestar.myvibedapp.commute.domain;

public record TransitOrigin(double latitude, double longitude) {

    public TransitOrigin {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Origin latitude must be between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Origin longitude must be between -180 and 180");
        }
    }
}
