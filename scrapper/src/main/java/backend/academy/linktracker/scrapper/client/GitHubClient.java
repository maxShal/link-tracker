package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.github.GitHubRepositoryResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
public class GitHubClient {

    private final RestClient githubRestClient;

    public GitHubRepositoryResponse getRepository(String owner, String repo) {
        return githubRestClient
                .get()
                .uri("/repos/{owner}/{repo}", owner, repo)
                .retrieve()
                .body(GitHubRepositoryResponse.class);
    }
}
