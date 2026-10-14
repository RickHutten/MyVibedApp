package nl.codestar.myvibedapp.commute.domain;

import java.time.LocalTime;

public record CommutePreferences(
        long wakeUpLeadMinutes,
        long transitAccessBufferMinutes,
        long officeArrivalLeadMinutes,
        LocalTime visibilityCutoff) {

    public CommutePreferences(
            final long wakeUpLeadMinutes,
            final long transitAccessBufferMinutes,
            final long officeArrivalLeadMinutes) {
        this(wakeUpLeadMinutes, transitAccessBufferMinutes, officeArrivalLeadMinutes, LocalTime.NOON);
    }

    public CommutePreferences {
        if (wakeUpLeadMinutes < 0 || transitAccessBufferMinutes < 0 || officeArrivalLeadMinutes < 0) {
            throw new IllegalArgumentException("Commute timing preferences must not be negative");
        }
        if (LocalTime.MIDNIGHT.equals(visibilityCutoff)) {
            throw new IllegalArgumentException("Commute visibility cutoff must be after midnight");
        }
    }

    public static CommutePreferences defaults() {
        return new CommutePreferences(45, 5, 5, LocalTime.NOON);
    }
}
