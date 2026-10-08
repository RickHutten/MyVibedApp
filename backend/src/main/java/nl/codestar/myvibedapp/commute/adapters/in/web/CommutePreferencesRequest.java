package nl.codestar.myvibedapp.commute.adapters.in.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record CommutePreferencesRequest(
        @NotNull @DecimalMin("0") @DecimalMax("9223372036854775807") @Digits(integer = 19, fraction = 0)
                BigDecimal wakeUpLeadMinutes,
        @NotNull @DecimalMin("0") @DecimalMax("9223372036854775807") @Digits(integer = 19, fraction = 0)
                BigDecimal transitAccessBufferMinutes,
        @NotNull @DecimalMin("0") @DecimalMax("9223372036854775807") @Digits(integer = 19, fraction = 0)
                BigDecimal officeArrivalLeadMinutes) {
}
