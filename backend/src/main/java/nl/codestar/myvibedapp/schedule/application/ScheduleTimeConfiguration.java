package nl.codestar.myvibedapp.schedule.application;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ScheduleTimeConfiguration {

    private static final ZoneId SCHEDULE_ZONE = ZoneId.of("Europe/Amsterdam");

    @Bean
    Clock scheduleClock() {
        return Clock.system(SCHEDULE_ZONE);
    }
}
