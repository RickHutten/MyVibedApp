package nl.codestar.myvibedapp.schedule.adapters.in.web;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

record OneOffOverrideRequest(
        @NotNull LocalDate startDate,
        @Nullable LocalDate endDate,
        @NotNull String status,
        @Nullable UUID officeId) {
}
