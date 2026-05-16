package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.configuration.properties.GithubProperties;
import backend.academy.linktracker.scrapper.model.github.GitHubRepositoryResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
@Slf4j
public class GitHubClient {

    private final RestClient githubRestClient;

    private final GithubProperties githubProperties;

    @Retry(name = "githubRetry", fallbackMethod = "getRepositoryIssuesFallback")
    public GitHubRepositoryResponse[] getRepositoryIssues(String owner, String repo) {
        return githubRestClient
                .get()
                .uri(githubProperties.getUrlEndpoint(), owner, repo)
                .retrieve()
                .body(GitHubRepositoryResponse[].class);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private GitHubRepositoryResponse[] getRepositoryIssuesFallback(String owner, String repo, Exception exception) {
        log.warn("GitHub fallback was called for repository {}/{}", owner, repo, exception);
        return new GitHubRepositoryResponse[0];
    }
}
