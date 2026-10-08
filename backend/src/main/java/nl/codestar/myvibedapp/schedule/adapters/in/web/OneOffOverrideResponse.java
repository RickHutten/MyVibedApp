package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.time.LocalDate;
import java.util.UUID;
import nl.codestar.myvibedapp.schedule.domain.OneOffOverride;
import org.jspecify.annotations.Nullable;

record OneOffOverrideResponse(
        UUID id,
        LocalDate startDate,
        @Nullable LocalDate endDate,
        String status,
        @Nullable UUID officeId) {

    static OneOffOverrideResponse from(final OneOffOverride override) {
        return new OneOffOverrideResponse(
                override.id(), override.startDate(), override.endDate(), override.status().name(), override.officeId());
    }
}
