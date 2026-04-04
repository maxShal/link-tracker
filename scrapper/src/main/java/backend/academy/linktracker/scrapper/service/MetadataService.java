package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackoverflowClient;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import backend.academy.linktracker.scrapper.util.Utils;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MetadataService {

    private final GitHubClient githubClient;
    private final StackoverflowClient stackoverflowClient;

    private static final Pattern GITHUB_PATTERN = Pattern.compile(Utils.GITHUB);

    private static final Pattern STACKOVERFLOW_PATTERN = Pattern.compile(Utils.STACKOVERFLOW);

    public Optional<LinkUpdateResponse> getLastUpdated(String url) {
        Matcher githubMatcher = GITHUB_PATTERN.matcher(url);
        if (githubMatcher.matches()) {
            String owner = githubMatcher.group(1);
            String repo = githubMatcher.group(2);

            var response = githubClient.getRepository(owner, repo);
            return Arrays.stream(response)
                    .filter(item -> item.createdAt() != null)
                    .map(item ->
                            new LinkUpdateResponse(item.title(), item.user().login(), item.createdAt(), item.body()))
                    .findFirst();
        }

        /*        Matcher stackMatcher = STACKOVERFLOW_PATTERN.matcher(url);
        if (stackMatcher.matches()) {
            Long questionId = Long.parseLong(stackMatcher.group(1));

            var response = stackoverflowClient.getQuestion(questionId);
            if (response.items() == null || response.items().isEmpty()) {
                throw new IllegalArgumentException("Вопрос не найден: " + url);
            }

            return Instant.ofEpochSecond(response.items().get(0).lastActivityDate());
        }*/

        throw new IllegalArgumentException("Неподдерживаемая ссылка: " + url);
    }
}
