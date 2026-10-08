package nl.codestar.myvibedapp.commute.domain;

public record CommutePreferences(
        long wakeUpLeadMinutes,
        long transitAccessBufferMinutes,
        long officeArrivalLeadMinutes) {

    public CommutePreferences {
        if (wakeUpLeadMinutes < 0 || transitAccessBufferMinutes < 0 || officeArrivalLeadMinutes < 0) {
            throw new IllegalArgumentException("Commute timing preferences must not be negative");
        }
    }

    public static CommutePreferences defaults() {
        return new CommutePreferences(45, 5, 5);
    }
}
