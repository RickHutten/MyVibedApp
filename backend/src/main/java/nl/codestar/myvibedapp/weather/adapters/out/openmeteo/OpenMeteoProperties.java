package nl.codestar.myvibedapp.weather.adapters.out.openmeteo;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather.open-meteo")
public record OpenMeteoProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout) {
}
