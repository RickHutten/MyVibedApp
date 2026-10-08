package nl.codestar.myvibedapp.support.postgresql;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class FixedScheduleClockConfiguration {

    private static final ZoneId SCHEDULE_ZONE = ZoneId.of("Europe/Amsterdam");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-10-07T10:00:00Z");

    @Bean
    @Primary
    Clock fixedScheduleClock() {
        return Clock.fixed(FIXED_INSTANT, SCHEDULE_ZONE);
    }
}
