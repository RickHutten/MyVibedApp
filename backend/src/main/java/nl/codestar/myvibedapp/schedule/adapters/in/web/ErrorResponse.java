package nl.codestar.myvibedapp.schedule.adapters.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
record ErrorResponse(
        String detail,
        @Nullable UUID conflictRuleId,
        @Nullable UUID conflictOverrideId) {

    ErrorResponse(final String detail) {
        this(detail, null, null);
    }

    ErrorResponse(final String detail, final UUID conflictRuleId) {
        this(detail, conflictRuleId, null);
    }
}
