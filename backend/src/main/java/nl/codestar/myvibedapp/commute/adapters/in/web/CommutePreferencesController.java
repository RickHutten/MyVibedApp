package nl.codestar.myvibedapp.commute.adapters.in.web;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.commute.application.CommutePreferencesService;
import nl.codestar.myvibedapp.commute.domain.CommutePreferences;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commute/preferences")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class CommutePreferencesController {

    private final CommutePreferencesService service;

    @GetMapping
    CommutePreferencesResponse get() {
        return CommutePreferencesResponse.from(service.get());
    }

    @PutMapping
    CommutePreferencesResponse replace(@Valid @RequestBody final CommutePreferencesRequest request) {
        final CommutePreferences preferences = new CommutePreferences(
                wholeMinutes(request.wakeUpLeadMinutes()),
                wholeMinutes(request.transitAccessBufferMinutes()),
                wholeMinutes(request.officeArrivalLeadMinutes()));
        return CommutePreferencesResponse.from(service.replace(preferences));
    }

    private long wholeMinutes(final BigDecimal value) {
        return value.longValueExact();
    }
}
