package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.configuration.properties.GithubProperties;
import backend.academy.linktracker.scrapper.model.github.GitHubRepositoryResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
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

    private GitHubRepositoryResponse[] getRepositoryIssuesFallback(String owner, String repo, Exception exception) {
        return new GitHubRepositoryResponse[0];
    }
}
