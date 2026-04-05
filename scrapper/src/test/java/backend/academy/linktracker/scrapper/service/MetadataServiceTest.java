package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackoverflowClient;
import backend.academy.linktracker.scrapper.model.github.GitHubRepositoryResponse;
import backend.academy.linktracker.scrapper.model.github.GitHubUserResponse;
import backend.academy.linktracker.scrapper.model.stackoverflow.StackoverflowRepositoryResponse;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

    private static final String GIT_LINK = "https://github.com/owner/repo";
    private static final String STACKOVERFLOW_LINK = "https://stackoverflow.com/questions/123/test-title";

    @Mock
    private GitHubClient githubClient;

    @Mock
    private StackoverflowClient stackoverflowClient;

    @InjectMocks
    private MetadataService metadataService;

    @Test
    void shouldReturnInstantForGithubUrl() {
        String updated = "2026-03-10T14:39:32Z";
        when(githubClient.getRepository("owner", "repo")).thenReturn(new GitHubRepositoryResponse[] {
            new GitHubRepositoryResponse(new GitHubUserResponse("user"), updated, "title", "body")
        });

        Instant result = OffsetDateTime.parse(
                        metadataService.getLastUpdated(GIT_LINK).get().createdAt())
                .toInstant();

        assertEquals(OffsetDateTime.parse(updated).toInstant(), result);
    }

    @Test
    void shouldReturnInstantForStackoverflowUrl() {
        long epoch = 1710000000L;
        long questionId = 123L;

        when(stackoverflowClient.getQuestion(questionId))
                .thenReturn(new StackoverflowRepositoryResponse.StackoverflowQuestionResponse(
                        List.of(new StackoverflowRepositoryResponse.QuestionItem(questionId, "Test title"))));
        var answerItem = new StackoverflowRepositoryResponse.AnswerItem(
                1L, epoch, "body", new StackoverflowRepositoryResponse.Owner("display_name"));
        var answerItems = new StackoverflowRepositoryResponse.AnswersResponse(List.of(answerItem));

        when(stackoverflowClient.getAnswers(questionId)).thenReturn(answerItems);
        Instant result = OffsetDateTime.parse(
                        metadataService.getLastUpdated(STACKOVERFLOW_LINK).get().createdAt())
                .toInstant();
        //
        assertEquals(Instant.ofEpochSecond(epoch), result);
    }

    @Test
    void shouldThrowForUnsupportedUrl() {
        assertThrows(IllegalArgumentException.class, () -> metadataService.getLastUpdated("https://example.com/test"));
    }

    @Test
    void shouldThrowWhenStackoverflowItemsEmpty() {
        when(stackoverflowClient.getQuestion(123L))
                .thenReturn(new StackoverflowRepositoryResponse.StackoverflowQuestionResponse(List.of()));

        assertThrows(IllegalArgumentException.class, () -> metadataService.getLastUpdated(STACKOVERFLOW_LINK));
    }
}
