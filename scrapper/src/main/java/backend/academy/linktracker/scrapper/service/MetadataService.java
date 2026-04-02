package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackoverflowClient;
import backend.academy.linktracker.scrapper.util.Utils;
import java.time.Instant;
import java.time.OffsetDateTime;
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

    public Instant getLastUpdated(String url) {
        Matcher githubMatcher = GITHUB_PATTERN.matcher(url);
        if (githubMatcher.matches()) {
            String owner = githubMatcher.group(1);
            String repo = githubMatcher.group(2);

            var response = githubClient.getRepository(owner, repo);
            return OffsetDateTime.parse(response.pushedAt()).toInstant();
        }

        Matcher stackMatcher = STACKOVERFLOW_PATTERN.matcher(url);
        if (stackMatcher.matches()) {
            Long questionId = Long.parseLong(stackMatcher.group(1));

            var response = stackoverflowClient.getQuestion(questionId);
            if (response.items() == null || response.items().isEmpty()) {
                throw new IllegalArgumentException("Вопрос не найден: " + url);
            }

            return Instant.ofEpochSecond(response.items().get(0).lastActivityDate());
        }

        throw new IllegalArgumentException("Неподдерживаемая ссылка: " + url);
    }
}
