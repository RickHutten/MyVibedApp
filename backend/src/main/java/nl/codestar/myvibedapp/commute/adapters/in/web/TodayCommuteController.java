package nl.codestar.myvibedapp.commute.adapters.in.web;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.codestar.myvibedapp.commute.application.TodayCommuteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-schedule/commute")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class TodayCommuteController {

    private final TodayCommuteService service;

    @GetMapping("/today")
    TodayCommuteResponse getTodayCommute(
            @RequestParam final double latitude,
            @RequestParam final double longitude) {
        return TodayCommuteResponse.from(service.get(latitude, longitude));
    }
}
