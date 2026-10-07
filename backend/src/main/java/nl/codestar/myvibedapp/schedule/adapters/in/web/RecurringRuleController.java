package nl.codestar.myvibedapp.schedule.adapters.in.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.schedule.application.RecurringRuleConflictException;
import nl.codestar.myvibedapp.schedule.application.RecurringRuleService;
import nl.codestar.myvibedapp.schedule.domain.RecurringRule;
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
@RequestMapping("/api/work-schedule/recurring-rules")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class RecurringRuleController {

    private final RecurringRuleService service;

    @GetMapping
    List<RecurringRuleResponse> list() {
        return service.rules().stream().map(RecurringRuleResponse::from).toList();
    }

    @PostMapping
    ResponseEntity<RecurringRuleResponse> add(@Valid @RequestBody final RecurringRuleRequest request) {
        final RecurringRule rule = RecurringRuleRequestMapper.toDomain(UUID.randomUUID(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RecurringRuleResponse.from(service.add(rule)));
    }

    @PutMapping("/{id}")
    RecurringRuleResponse edit(
            @PathVariable final UUID id,
            @Valid @RequestBody final RecurringRuleRequest request) {
        final RecurringRule rule = RecurringRuleRequestMapper.toDomain(id, request);
        return RecurringRuleResponse.from(service.edit(id, rule));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable final UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RecurringRuleConflictException.class)
    ResponseEntity<ErrorResponse> handleConflict(final RecurringRuleConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        exception.getMessage() == null ? "The recurring rule overlaps an existing rule" : exception.getMessage(),
                        exception.conflictingRuleId()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleValidation(final IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                exception.getMessage() == null ? "Invalid recurring rule request" : exception.getMessage()));
    }
}
