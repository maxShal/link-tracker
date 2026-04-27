package backend.academy.linktracker.scrapper;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(properties = "app.message-transport=http")
public abstract class AbstractIntegrationTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:3.9.1")
            .withExposedPorts(8080)
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("app.github.url", () -> "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080));
        registry.add(
                "app.stackoverflow.url", () -> "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080));
        registry.add("app.stackoverflow.key", () -> "");
    }
}
