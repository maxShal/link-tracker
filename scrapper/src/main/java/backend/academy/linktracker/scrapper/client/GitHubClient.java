package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.configuration.properties.GithubProperties;
import backend.academy.linktracker.scrapper.model.github.GitHubRepositoryResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
public class GitHubClient {

    private final RestClient githubRestClient;

    private final GithubProperties githubProperties;

    public GitHubRepositoryResponse getRepository(String owner, String repo) {
        return githubRestClient
                .get()
                .uri(githubProperties.getUrlEndpoint(), owner, repo)
                .retrieve()
                .body(GitHubRepositoryResponse.class);
    }
}
