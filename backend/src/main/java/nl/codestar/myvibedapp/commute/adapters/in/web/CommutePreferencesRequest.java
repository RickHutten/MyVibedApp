package nl.codestar.myvibedapp.commute.adapters.in.web;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalTime;

record CommutePreferencesRequest(
        @NotNull @PositiveOrZero Long wakeUpLeadMinutes,
        @NotNull @PositiveOrZero Long transitAccessBufferMinutes,
        @NotNull @PositiveOrZero Long officeArrivalLeadMinutes,
        @NotNull LocalTime visibilityCutoff) {
}
