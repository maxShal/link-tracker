package backend.academy.linktracker.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.configuration.properties.GithubProperties;
import backend.academy.linktracker.scrapper.model.github.GitHubRepositoryResponse;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.Comparator;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    classes = GitHubClientRetryTest.TestApplication.class,
    properties = {
        "resilience4j.retry.instances.githubRetry.maxAttempts=3",
        "resilience4j.retry.instances.githubRetry.waitDuration=200ms",
        "resilience4j.retry.instances.githubRetry.retryExceptions=org.springframework.web.client.HttpServerErrorException,org.springframework.web.client.ResourceAccessException",
        "resilience4j.retry.instances.githubRetry.ignoreExceptions=org.springframework.web.client.HttpClientErrorException"
    }
)
class GitHubClientRetryTest {

    @RegisterExtension
    static WireMockExtension github = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @Autowired
    private GitHubClient gitHubClient;

    @MockitoBean
    private GithubProperties githubProperties;

    @BeforeEach
    void setUp() {
        github.resetAll();

        when(githubProperties.getUrlEndpoint())
            .thenReturn("/repos/{owner}/{repo}/issues");
    }

    @Test
    void shouldRetryOn5xxAndReturnSuccessfulResponse() {
        github.stubFor(get(urlEqualTo("/repos/test-owner/test-repo/issues"))
            .inScenario("github retry")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(serverError())
            .willSetStateTo("second failure"));

        github.stubFor(get(urlEqualTo("/repos/test-owner/test-repo/issues"))
            .inScenario("github retry")
            .whenScenarioStateIs("second failure")
            .willReturn(serverError())
            .willSetStateTo("success"));

        github.stubFor(get(urlEqualTo("/repos/test-owner/test-repo/issues"))
            .inScenario("github retry")
            .whenScenarioStateIs("success")
            .willReturn(okJson("[]")));

        GitHubRepositoryResponse[] response =
            gitHubClient.getRepositoryIssues("test-owner", "test-repo");

        assertNotNull(response);
        assertEquals(0, response.length);

        github.verify(3, getRequestedFor(urlEqualTo("/repos/test-owner/test-repo/issues")));
    }

    @Test
    void shouldNotRetryOn4xx() {
        github.stubFor(get(urlEqualTo("/repos/test-owner/test-repo/issues"))
            .willReturn(badRequest()));

        GitHubRepositoryResponse[] response =
            gitHubClient.getRepositoryIssues("test-owner", "test-repo");

        assertNotNull(response);
        assertEquals(0, response.length);

        github.verify(1, getRequestedFor(urlEqualTo("/repos/test-owner/test-repo/issues")));
    }

    @Test
    void shouldRespectConstantRetryInterval() {
        github.stubFor(get(urlEqualTo("/repos/test-owner/test-repo/issues"))
            .inScenario("github retry interval")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(serverError())
            .willSetStateTo("second failure"));

        github.stubFor(get(urlEqualTo("/repos/test-owner/test-repo/issues"))
            .inScenario("github retry interval")
            .whenScenarioStateIs("second failure")
            .willReturn(serverError())
            .willSetStateTo("success"));

        github.stubFor(get(urlEqualTo("/repos/test-owner/test-repo/issues"))
            .inScenario("github retry interval")
            .whenScenarioStateIs("success")
            .willReturn(okJson("[]")));

        gitHubClient.getRepositoryIssues("test-owner", "test-repo");

        var events = github.getAllServeEvents().stream()
            .filter(event -> event.getRequest().getUrl().equals("/repos/test-owner/test-repo/issues"))
            .sorted(Comparator.comparing(event -> event.getRequest().getLoggedDate()))
            .toList();

        assertEquals(3, events.size());

        long firstInterval = events.get(1).getRequest().getLoggedDate().getTime()
            - events.get(0).getRequest().getLoggedDate().getTime();

        long secondInterval = events.get(2).getRequest().getLoggedDate().getTime()
            - events.get(1).getRequest().getLoggedDate().getTime();

        assertTrue(firstInterval >= 180, "Первый retry interval должен быть около 200ms");
        assertTrue(secondInterval >= 180, "Второй retry interval должен быть около 200ms");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        LiquibaseAutoConfiguration.class
    })
    @Import(GitHubClient.class)
    static class TestApplication {

        @Bean
        RestClient githubRestClient() {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(200));
            requestFactory.setReadTimeout(Duration.ofMillis(500));

            return RestClient.builder()
                .baseUrl(github.baseUrl())
                .requestFactory(requestFactory)
                .build();
        }
    }
}
