package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

record ErrorResponse(String detail, @Nullable UUID conflictRuleId) {

    ErrorResponse(final String detail) {
        this(detail, null);
    }
}
