package backend.academy.linktracker.scrapper.service.strateges;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.exception.errors.GitHubMatchException;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import backend.academy.linktracker.scrapper.util.Utils;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitHubStrategy implements IStrategyHandler {

    private final GitHubClient githubClient;

    private static final Pattern GITHUB_PATTERN = Pattern.compile(Utils.GITHUB);

    @Override
    public Optional<LinkUpdateResponse> getLastUpdated(String url) {
        if (url.isBlank()) return Optional.empty();
        Matcher githubMatcher = GITHUB_PATTERN.matcher(url);
        if (!githubMatcher.matches()) throw new GitHubMatchException("Ссылка " + url + " не поддерживается GitHub");

        String owner = githubMatcher.group(1);
        String repo = githubMatcher.group(2);

        var responses = githubClient.getRepositoryIssues(owner, repo);
        return Arrays.stream(responses)
                .filter(item -> item.createdAt() != null)
                .map(item -> new LinkUpdateResponse(item.title(), item.user().login(), item.createdAt(), item.body()))
                .findFirst();
    }

    @Override
    public boolean patternCheck(String url) {
        return GITHUB_PATTERN.matcher(url).matches();
    }
}
