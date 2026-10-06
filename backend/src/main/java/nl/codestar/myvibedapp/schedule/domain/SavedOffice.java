package nl.codestar.myvibedapp.schedule.domain;

import java.util.UUID;

public record SavedOffice(
        UUID id,
        String label,
        String address,
        double latitude,
        double longitude,
        boolean deleted) {

    private static final int MAX_LABEL_LENGTH = 120;
    private static final int MAX_ADDRESS_LENGTH = 500;

    public SavedOffice {
        if (label.isBlank()) {
            throw new IllegalArgumentException("Office label is required");
        }
        if (label.length() > MAX_LABEL_LENGTH) {
            throw new IllegalArgumentException("Office label must be at most 120 characters");
        }
        if (address.isBlank()) {
            throw new IllegalArgumentException("Office address is required");
        }
        if (address.length() > MAX_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("Office address must be at most 500 characters");
        }
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Office latitude must be between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Office longitude must be between -180 and 180");
        }
    }

    public SavedOffice update(final String label, final String address, final double latitude, final double longitude) {
        return new SavedOffice(id, label, address, latitude, longitude, deleted);
    }

    public SavedOffice softDelete() {
        return new SavedOffice(id, label, address, latitude, longitude, true);
    }
}
