package nl.codestar.myvibedapp.support.postgresql;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class PostgresIntegrationTest {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.6");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected MockMvc mockMvc;

    @DynamicPropertySource
    static void configurePostgres(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void resetApplicationDatabase() {
        mockMvc = webAppContextSetup(context).build();
        new ApplicationDatabaseCleaner(jdbcTemplate).resetScheduleData();
    }
}
