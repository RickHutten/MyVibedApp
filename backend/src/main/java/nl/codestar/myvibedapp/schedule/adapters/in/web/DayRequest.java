package nl.codestar.myvibedapp.schedule.adapters.in.web;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

record DayRequest(
        @NotBlank String day,
        @NotBlank String status,
        UUID officeId) {
}
