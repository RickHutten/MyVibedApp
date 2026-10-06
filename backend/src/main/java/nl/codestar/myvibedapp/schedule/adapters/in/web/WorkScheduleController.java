package nl.codestar.myvibedapp.schedule.adapters.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.schedule.application.GeocodingUnavailableException;
import nl.codestar.myvibedapp.schedule.application.WorkScheduleService;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.WorkSchedule;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class WorkScheduleController {

    private final WorkScheduleService service;

    @GetMapping("/work-schedule")
    WorkScheduleResponse getSchedule() {
        return response(service.getSchedule());
    }

    @PutMapping("/work-schedule")
    WorkScheduleResponse replaceSchedule(@Valid @RequestBody final WorkScheduleRequest request) {
        final WorkSchedule schedule = WorkScheduleRequestMapper.toDomain(request);
        return response(service.replaceSchedule(schedule));
    }

    @GetMapping("/offices")
    List<OfficeResponse> offices(@RequestParam(defaultValue = "false") final boolean includeDeleted) {
        return service.offices(includeDeleted).stream().map(OfficeResponse::from).toList();
    }

    @PostMapping("/offices")
    ResponseEntity<OfficeResponse> addOffice(@Valid @RequestBody final OfficeRequest request) {
        final SavedOffice office = service.addOffice(request.label(), request.address(), request.latitude(), request.longitude());
        return ResponseEntity.status(HttpStatus.CREATED).body(OfficeResponse.from(office));
    }

    @PutMapping("/offices/{id}")
    OfficeResponse editOffice(@PathVariable final UUID id, @Valid @RequestBody final OfficeRequest request) {
        return OfficeResponse.from(service.editOffice(id, request.label(), request.address(), request.latitude(), request.longitude()));
    }

    @DeleteMapping("/offices/{id}")
    ResponseEntity<Void> deleteOffice(@PathVariable final UUID id) {
        service.softDeleteOffice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/offices/search")
    List<AddressSuggestionResponse> searchOffices(
            @RequestParam @Size(max = 200, message = "Address search query must be at most 200 characters") final String query) {
        return AddressSuggestionResponse.from(service.searchOffices(query));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleValidation(final IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                exception.getMessage() == null ? "Invalid schedule request" : exception.getMessage()));
    }

    @ExceptionHandler(GeocodingUnavailableException.class)
    ResponseEntity<ErrorResponse> handleGeocodingUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Address search is unavailable"));
    }

    private WorkScheduleResponse response(final WorkSchedule schedule) {
        return WorkScheduleResponse.from(schedule, service.offices(true));
    }
}
