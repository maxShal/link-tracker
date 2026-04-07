package backend.academy.linktracker.scrapper.service.strateges;

import backend.academy.linktracker.scrapper.client.StackoverflowClient;
import backend.academy.linktracker.scrapper.exception.errors.StackOverFlowMatchException;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import backend.academy.linktracker.scrapper.util.Utils;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackOverFlowStrategy implements IStrategyHandler {

    private final StackoverflowClient stackoverflowClient;

    private static final Pattern STACKOVERFLOW_PATTERN = Pattern.compile(Utils.STACKOVERFLOW);

    @Override
    public Optional<LinkUpdateResponse> getLastUpdated(String url) {
        Matcher stackMatcher = STACKOVERFLOW_PATTERN.matcher(url);
        if (!stackMatcher.matches())
            throw new StackOverFlowMatchException("Ссылка " + url + " не поддерживается StackOverFlow");
        if (url.isBlank()) return Optional.empty();
        Long questionId = Long.parseLong(stackMatcher.group(1));

        var QuestionResponse = stackoverflowClient.getQuestion(questionId);
        if (QuestionResponse.items() == null || QuestionResponse.items().isEmpty()) {
            throw new IllegalArgumentException("Вопрос не найден: " + url);
        }

        String title = QuestionResponse.items().get(0).title();

        var answersResponse = stackoverflowClient.getAnswers(questionId);
        var commentResponse = stackoverflowClient.getComments(questionId);

        var answerStream = answersResponse != null
                ? answersResponse.items().stream()
                        .filter(item -> item.creationDate() != null)
                        .map(item -> new LinkUpdateResponse(
                                title,
                                item.owner().displayName(),
                                Instant.ofEpochSecond(item.creationDate()).toString(),
                                item.body()))
                : Stream.<LinkUpdateResponse>empty();

        var commentStream = commentResponse != null
                ? commentResponse.items().stream()
                        .filter(item -> item.creationDate() != null)
                        .map(item -> new LinkUpdateResponse(
                                title,
                                item.owner().displayName(),
                                Instant.ofEpochSecond(item.creationDate()).toString(),
                                item.body()))
                : Stream.<LinkUpdateResponse>empty();

        return Stream.concat(answerStream, commentStream).findFirst();
    }

    @Override
    public boolean patternCheck(String url) {
        return STACKOVERFLOW_PATTERN.matcher(url).matches();
    }
}
