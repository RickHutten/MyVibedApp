package nl.codestar.myvibedapp.schedule.adapters.in.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.schedule.application.OneOffOverrideConflictException;
import nl.codestar.myvibedapp.schedule.application.OneOffOverrideService;
import nl.codestar.myvibedapp.schedule.domain.OneOffOverride;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-schedule/overrides")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class OneOffOverrideController {

    private final OneOffOverrideService service;

    @GetMapping
    List<OneOffOverrideResponse> list() {
        return service.overrides().stream().map(OneOffOverrideResponse::from).toList();
    }

    @PostMapping
    ResponseEntity<OneOffOverrideResponse> add(@Valid @RequestBody final OneOffOverrideRequest request) {
        final OneOffOverride override = OneOffOverrideRequestMapper.toDomain(UUID.randomUUID(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OneOffOverrideResponse.from(service.add(override)));
    }

    @PutMapping("/{id}")
    OneOffOverrideResponse edit(
            @PathVariable final UUID id,
            @Valid @RequestBody final OneOffOverrideRequest request) {
        final OneOffOverride override = OneOffOverrideRequestMapper.toDomain(id, request);
        return OneOffOverrideResponse.from(service.edit(id, override));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable final UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(OneOffOverrideConflictException.class)
    ResponseEntity<ErrorResponse> handleConflict(final OneOffOverrideConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        exception.getMessage() == null ? "An override already covers part of that date range" : exception.getMessage(),
                        null,
                        exception.conflictingOverrideId()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleValidation(final IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                exception.getMessage() == null ? "Invalid one-off override request" : exception.getMessage()));
    }
}
