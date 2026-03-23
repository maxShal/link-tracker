package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.stackoverflow.StackoverflowRepositoryResponse;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
public class StackoverflowClient {

    private final StackoverflowProperties stackoverflowProperties;
    private final RestClient stackoverflowRestClient;

    public StackoverflowRepositoryResponse.StackoverflowQuestionResponse getQuestion(Long questionId) {
        return stackoverflowRestClient
                .get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path(stackoverflowProperties.getUrlPathEndpoint())
                            .queryParam("site", "stackoverflow");

                    if (stackoverflowProperties.getKey() != null
                            && !stackoverflowProperties.getKey().isBlank()) {
                        builder = builder.queryParam("key", stackoverflowProperties.getKey());
                    }

                    return builder.build(questionId);
                })
                .retrieve()
                .body(StackoverflowRepositoryResponse.StackoverflowQuestionResponse.class);
    }
}
