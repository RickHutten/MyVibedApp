package nl.codestar.myvibedapp.schedule.application;

import java.util.UUID;

public final class OneOffOverrideConflictException extends RuntimeException {

    private final UUID conflictingOverrideId;

    public OneOffOverrideConflictException(final UUID conflictingOverrideId) {
        super("An override already covers part of that date range");
        this.conflictingOverrideId = conflictingOverrideId;
    }

    public UUID conflictingOverrideId() {
        return conflictingOverrideId;
    }
}
