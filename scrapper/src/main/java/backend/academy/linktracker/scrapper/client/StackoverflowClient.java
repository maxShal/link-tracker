package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.stackoverflow.StackoverflowRepositoryResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
public class StackoverflowClient {

    private final RestClient stackoverflowRestClient;

    public StackoverflowRepositoryResponse.StackoverflowQuestionResponse getQuestion(Long questionId) {
        return stackoverflowRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/2.3/questions/{id}")
                        .queryParam("site", "stackoverflow")
                        .build(questionId))
                .retrieve()
                .body(StackoverflowRepositoryResponse.StackoverflowQuestionResponse.class);
    }
}
