package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
public class StackOverFlowIntegrationTest {

    private static final String URL = "https://stackoverflow.com/questions/123/test-title";

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
        registry.add("app.stackoverflow.key", () -> "");
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
    void shouldSendNewAnswer() throws InterruptedException {
        long chatId = 1L;
        tgChatService.addChat(chatId);

        LinkResponse linkResponse = linksService.addLink(chatId, new AddLinkRequest(URL, List.of()));
        linksService.updateLastUpdated(linkResponse.id(), Instant.parse("2020-01-01T00:00:00Z"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/2.3/questions/123"))
                .withQueryParam("site", WireMock.equalTo("ru.stackoverflow"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                {
                  "items": [
                    {
                      "question_id": 123,
                      "title": "title"
                    }
                  ]
                }
                """)));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/2.3/questions/123/answers"))
                .withQueryParam("site", WireMock.equalTo("ru.stackoverflow"))
                .withQueryParam("filter", WireMock.equalTo("withbody"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                      "items": [
                        {
                          "owner": {
                            "display_name": "hatirlatici"
                          },
                          "creation_date": 1767579672,
                          "answer_id": 79860452,
                          "body": "it is body"
                        }
                      ]
                    }
                    """)));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/2.3/questions/123/comments"))
                .withQueryParam("site", WireMock.equalTo("ru.stackoverflow"))
                .withQueryParam("filter", WireMock.equalTo("withbody"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "items": []
                            }
                            """)));

        scheduler.checkUpdates();

        ArgumentCaptor<LinkUpdateRequest> captor = ArgumentCaptor.forClass(LinkUpdateRequest.class);

        verify(sender).sendUpdate(captor.capture());

        LinkUpdateRequest request = captor.getValue();
        assertTrue(request.description().contains("title"));
        assertTrue(request.description().contains("hatirlatici"));
        assertTrue(request.description().contains("it is body"));
    }
}
