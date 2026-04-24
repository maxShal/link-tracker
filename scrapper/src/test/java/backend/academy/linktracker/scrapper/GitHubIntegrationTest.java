package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.model.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.model.response.LinkResponse;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class GitHubIntegrationTest {

    private static final String URL = "https://github.com/owner/repo";

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest")
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
    }

    @Autowired
    private LinkUpdaterScheduler scheduler;

    @Autowired
    private LinksService linksService;

    @Autowired
    private TgChatService tgChatService;

    @MockitoBean
    private BotClient sender;

    @BeforeEach
    void setUpWireMock() {
        WireMock.configureFor(wiremock.getHost(), wiremock.getMappedPort(8080));
        WireMock.reset();
    }

    @Test
    void shouldSendGithubIssueUpdate() throws InterruptedException {
        long chatId = 1L;
        tgChatService.addChat(chatId);

        LinkResponse linkResponse = linksService.addLink(chatId, new AddLinkRequest(URL, List.of()));
        linksService.updateLastUpdated(linkResponse.id(), Instant.parse("2020-01-01T00:00:00Z"));

        WireMock.stubFor(get(urlEqualTo("/repos/owner/repo/issues"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "title": "New issue",
                                    "body": "Issue description",
                                    "created_at": "2026-04-07T10:00:00Z",
                                    "user": { "login": "octocat" }
                                  }
                                ]
                                """)));

        scheduler.checkUpdates();

        ArgumentCaptor<LinkUpdateRequest> captor = ArgumentCaptor.forClass(LinkUpdateRequest.class);

        verify(sender).sendUpdate(captor.capture());

        LinkUpdateRequest request = captor.getValue();
        assertTrue(request.description().contains("New issue"));
        assertTrue(request.description().contains("octocat"));
        assertTrue(request.description().contains("Issue description"));
    }
}
