package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(properties = "app.message-transport=http")
public abstract class AbstractIntegrationTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("app.github.url", wireMock::baseUrl);
        registry.add("app.stackoverflow.url", wireMock::baseUrl);
        registry.add("app.stackoverflow.key", () -> "");
    }

    @Autowired
    protected LinkUpdaterScheduler scheduler;

    @Autowired
    protected LinksService linksService;

    @Autowired
    protected TgChatService tgChatService;

    @MockitoBean
    protected BotClient sender;

    @BeforeEach
    void setUpWireMock() {
        wireMock.resetAll();
    }
}
