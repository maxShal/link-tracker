package backend.academy.linktracker.e2e;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

public class HttpIntegrationTest extends AbstractScrapperBotTest {
    private static final Network NETWORK = Network.newNetwork();

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withNetwork(NETWORK)
            .withNetworkAliases("postgres")
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Container
    static GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:3.9.1")
            .withExposedPorts(8080)
            .withNetwork(NETWORK)
            .withNetworkAliases("wiremock")
            .waitingFor(Wait.forListeningPort());

    @Container
    static GenericContainer<?> bot = new GenericContainer<>("linktracker-bot:latest")
            .withExposedPorts(8080)
            .withNetwork(NETWORK)
            .withNetworkAliases("bot")
            .withEnv("TELEGRAM_TOKEN", "test-token")
            .withEnv("APP_TELEGRAM_AUTO_START", "false")
            .withEnv("APP_TELEGRAM_URL", "http://wiremock:8080/bot")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).forStatusCode(200));

    @Container
    static GenericContainer<?> scrapper = new GenericContainer<>("linktracker-scrapper:latest")
            .withExposedPorts(8081)
            .withNetwork(NETWORK)
            .withNetworkAliases("scrapper")
            .dependsOn(postgres, bot, wiremock)
            .withEnv("APP_BOT_URL", "http://bot:8080")
            .withEnv("SPRING_APPLICATION_JSON", """
            {
              "app": {
                "message-transport": "http"
              }
            }
            """)
            .withEnv("APP_GITHUB_URL", "http://wiremock:8080")
            .withEnv("APP_STACKOVERFLOW_URL", "http://wiremock:8080")
            .withEnv("GITHUB_TOKEN", "test-github-token")
            .withEnv("STACKOVERFLOW_KEY", "test-key")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/test")
            .withEnv("SPRING_DATASOURCE_USERNAME", "test")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "test")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8081).forStatusCode(200));

    @Override
    protected GenericContainer<?> scrapper() {
        return scrapper;
    }

    @Override
    protected GenericContainer<?> wiremock() {
        return wiremock;
    }
}
