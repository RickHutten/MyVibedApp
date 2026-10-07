package nl.codestar.myvibedapp.schedule.application;

import java.util.UUID;

public final class RecurringRuleConflictException extends RuntimeException {

    private final UUID conflictingRuleId;

    public RecurringRuleConflictException(final UUID conflictingRuleId) {
        super("The recurring rule overlaps an existing rule at the same recurrence level");
        this.conflictingRuleId = conflictingRuleId;
    }

    public UUID conflictingRuleId() {
        return conflictingRuleId;
    }
}
