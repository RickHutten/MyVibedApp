package nl.codestar.myvibedapp.schedule.adapters.in.web;

import jakarta.validation.constraints.NotBlank;

record WorkingHoursRequest(@NotBlank String start, @NotBlank String end) {
}
