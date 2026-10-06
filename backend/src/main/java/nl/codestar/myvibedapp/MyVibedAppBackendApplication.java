package nl.codestar.myvibedapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@SuppressWarnings("PMD.UseUtilityClass")
public class MyVibedAppBackendApplication {

	static void main(final String... args) {
		SpringApplication.run(MyVibedAppBackendApplication.class, args);
	}

}
