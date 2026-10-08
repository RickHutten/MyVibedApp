package nl.codestar.myvibedapp.schedule.adapters.in.web;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.schedule.application.UpcomingWorkScheduleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class UpcomingWorkScheduleController {

    private final UpcomingWorkScheduleService service;

    @GetMapping("/work-schedule/upcoming")
    UpcomingWorkScheduleResponse getUpcomingWorkSchedule() {
        return UpcomingWorkScheduleResponse.from(service.getUpcomingWorkSchedule());
    }
}
