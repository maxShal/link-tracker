package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.configuration.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.model.stackoverflow.StackoverflowRepositoryResponse;
import java.net.URI;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
@AllArgsConstructor
public class StackoverflowClient {

    private static final String SITE = "ru.stackoverflow";
    private static final String WITH_BODY_FILTER = "withbody";

    private final StackoverflowProperties stackoverflowProperties;
    private final RestClient stackoverflowRestClient;

    public StackoverflowRepositoryResponse.StackoverflowQuestionResponse getQuestion(Long questionId) {
        return stackoverflowRestClient
                .get()
                .uri(uriBuilder -> buildQuestionUri(uriBuilder, questionId))
                .retrieve()
                .body(StackoverflowRepositoryResponse.StackoverflowQuestionResponse.class);
    }

    public StackoverflowRepositoryResponse.AnswersResponse getAnswers(Long questionId) {
        return stackoverflowRestClient
                .get()
                .uri(uriBuilder -> buildAnswersUri(uriBuilder, questionId))
                .retrieve()
                .body(StackoverflowRepositoryResponse.AnswersResponse.class);
    }

    public StackoverflowRepositoryResponse.CommentsResponse getComments(Long questionId) {
        return stackoverflowRestClient
                .get()
                .uri(uriBuilder -> buildCommentsUri(uriBuilder, questionId))
                .retrieve()
                .body(StackoverflowRepositoryResponse.CommentsResponse.class);
    }

    private URI buildQuestionUri(UriBuilder uriBuilder, Long questionId) {
        UriBuilder builder =
                uriBuilder.path(stackoverflowProperties.getUrlPathEndpoint()).queryParam("site", SITE);

        if (hasKey()) {
            builder = builder.queryParam("key", stackoverflowProperties.getKey());
        }

        return builder.build(questionId);
    }

    private URI buildAnswersUri(UriBuilder uriBuilder, Long questionId) {
        UriBuilder builder = uriBuilder
                .path(stackoverflowProperties.getUrlPathAnswer())
                .queryParam("site", SITE)
                .queryParam("filter", WITH_BODY_FILTER);

        if (hasKey()) {
            builder = builder.queryParam("key", stackoverflowProperties.getKey());
        }

        return builder.build(questionId);
    }

    private URI buildCommentsUri(UriBuilder uriBuilder, Long questionId) {
        UriBuilder builder = uriBuilder
                .path(stackoverflowProperties.getUrlPathComment())
                .queryParam("site", SITE)
                .queryParam("filter", WITH_BODY_FILTER);

        if (hasKey()) {
            builder = builder.queryParam("key", stackoverflowProperties.getKey());
        }

        return builder.build(questionId);
    }

    private boolean hasKey() {
        return stackoverflowProperties.getKey() != null
                && !stackoverflowProperties.getKey().isBlank();
    }
}
