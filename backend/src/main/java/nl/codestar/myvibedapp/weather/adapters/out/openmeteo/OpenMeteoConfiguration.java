package nl.codestar.myvibedapp.weather.adapters.out.openmeteo;

import jakarta.validation.Validator;
import nl.codestar.myvibedapp.weather.application.WeatherProvider;
import nl.codestar.myvibedapp.weather.application.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
class OpenMeteoConfiguration {

    @Bean
    WeatherProvider openMeteoWeatherProvider(
            @Value("${weather.open-meteo.base-url}") String baseUrl,
            @Value("${weather.open-meteo.connect-timeout}") Duration connectTimeout,
            @Value("${weather.open-meteo.read-timeout}") Duration readTimeout,
            Validator validator) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        return new OpenMeteoWeatherProvider(restClient, validator);
    }

    @Bean
    WeatherService weatherService(WeatherProvider weatherProvider) {
        return new WeatherService(weatherProvider);
    }
}
