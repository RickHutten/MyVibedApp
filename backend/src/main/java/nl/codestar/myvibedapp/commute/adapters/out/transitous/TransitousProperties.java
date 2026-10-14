package nl.codestar.myvibedapp.commute.adapters.out.transitous;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commute.transitous")
public record TransitousProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {

    public TransitousProperties {
        if (baseUrl.isBlank()) {
            throw new IllegalArgumentException("Transitous base URL is required");
        }
        if (connectTimeout.isNegative() || connectTimeout.isZero()) {
            throw new IllegalArgumentException("Transitous connect timeout must be positive");
        }
        if (readTimeout.isNegative() || readTimeout.isZero()) {
            throw new IllegalArgumentException("Transitous read timeout must be positive");
        }
    }
}
