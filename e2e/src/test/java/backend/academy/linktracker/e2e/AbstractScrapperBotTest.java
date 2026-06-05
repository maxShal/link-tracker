package backend.academy.linktracker.e2e;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
public abstract class AbstractScrapperBotTest {
    protected static final Network NETWORK = Network.newNetwork();

    protected abstract GenericContainer<?> scrapper();

    @Container
    static GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:3.9.1")
            .withExposedPorts(8080)
            .withNetwork(NETWORK)
            .withNetworkAliases("wiremock")
            .waitingFor(Wait.forListeningPort());

    @Container
    static GenericContainer<?> valkey = new GenericContainer<>("valkey/valkey:latest")
            .withExposedPorts(6379)
            .withNetwork(NETWORK)
            .withNetworkAliases("valkey")
            .waitingFor(Wait.forListeningPort());

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withNetwork(NETWORK)
            .withNetworkAliases("postgres")
            .waitingFor(Wait.forListeningPort());

    @Test
    void scrapperSendUpdateAndBotSendTest() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();

        String scrapperBaseUrl =
                "http://" + scrapper().getHost() + ":" + scrapper().getMappedPort(8081);
        String wiremockBaseUrl = "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080);

        String githubStub = """
            {
              "request": {
                "method": "GET",
                "urlPath": "/repos/owner/repo/issues"
              },
              "response": {
                "status": 200,
                "headers": {
                  "Content-Type": "application/json"
                },
                "jsonBody": [
                  {
                    "title": "New issue from Kafka e2e",
                    "user": {
                      "login": "octocat"
                    },
                    "created_at": "2026-12-20T10:00:00Z",
                    "body": "Kafka notification body"
                  }
                ]
              }
            }
            """;

        var gitResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(wiremockBaseUrl + "/__admin/mappings"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(githubStub))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(201, gitResponse.statusCode());

        String telegramStub = """
            {
              "request": {
                "method": "POST",
                "urlPathPattern": "/bot[^/]+/sendMessage"
              },
              "response": {
                "status": 200,
                "headers": {
                  "Content-Type": "application/json"
                },
                "jsonBody": {
                  "ok": true,
                  "result": {
                    "message_id": 1
                  }
                }
              }
            }
            """;

        var telegramResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(wiremockBaseUrl + "/__admin/mappings"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(telegramStub))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(201, telegramResponse.statusCode());

        var registerChatResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(scrapperBaseUrl + "/tg-chat/1"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, registerChatResponse.statusCode());

        String addBody = """
            {
              "link": "https://github.com/owner/repo",
              "tags": []
            }
            """;

        var addLinkResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(scrapperBaseUrl + "/links"))
                        .header("Tg-Chat-Id", "1")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(addBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, addLinkResponse.statusCode());

        await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> {
            var request = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(wiremockBaseUrl + "/__admin/requests"))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, request.statusCode());
            assertTrue(request.body().contains("/repos/owner/repo/issues"));
        });

        await().atMost(Duration.ofSeconds(40)).untilAsserted(() -> {
            var request = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(wiremockBaseUrl + "/__admin/requests"))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            String body = request.body();

            assertEquals(200, request.statusCode());
            assertTrue(body.contains("Kafka notification body"));
            System.out.println(body);
            assertTrue(body.contains("New issue from Kafka e2e"));
        });
    }
}
