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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class IntegrationKafkaTest {
    private static final Network NETWORK = Network.newNetwork();

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.9.1"))
            .withNetwork(NETWORK)
            .withNetworkAliases("kafka")
            .withListener("kafka:19092");

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
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.message-send.topic", () -> "message-send");
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
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:19092")
            .withEnv("SPRING_KAFKA_CONSUMER_GROUP_ID", "bot-e2e-consumer")
            .withEnv("SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET", "earliest")
            .withEnv("APP_MESSAGE_SEND_TOPIC", "message-send")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).forStatusCode(200));

    @Container
    static GenericContainer<?> scrapper = new GenericContainer<>("linktracker-scrapper:latest")
            .withExposedPorts(8081)
            .withNetwork(NETWORK)
            .withNetworkAliases("scrapper")
            .dependsOn(postgres, kafka, wiremock, bot)
            .withEnv("APP_MESSAGE_TRANSPORT", "kafka")
            .withEnv("APP_SCHEDULER_CHECK", "1000")
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:19092")
            .withEnv("APP_MESSAGE_SEND_PARTITIONS", "1")
            .withEnv("APP_MESSAGE_SEND_REPLICAS", "1")
            .withEnv("APP_MESSAGE_SEND_TOPIC", "message-send")
            .withEnv("APP_GITHUB_URL", "http://wiremock:8080")
            .withEnv("APP_STACKOVERFLOW_URL", "http://wiremock:8080")
            .withEnv("GITHUB_TOKEN", "test-github-token")
            .withEnv("STACKOVERFLOW_KEY", "test-key")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/test")
            .withEnv("SPRING_DATASOURCE_USERNAME", "test")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "test")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8081).forStatusCode(200));

    @Test
    void scrapperSendUpdateAndBotSendTest() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        String scrapperBaseUrl = "http://" + scrapper.getHost() + ":" + scrapper.getMappedPort(8081);
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
                    "created_at": "2026-05-20T10:00:00Z",
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
            assertTrue(body.contains("sendMessage"));
            assertTrue(body.contains("New issue from Kafka e2e"));
        });
    }
}
