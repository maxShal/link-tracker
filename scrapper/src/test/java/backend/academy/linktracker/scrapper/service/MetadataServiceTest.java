package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackoverflowClient;
import backend.academy.linktracker.scrapper.model.stackoverflow.StackoverflowRepositoryResponse;
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
    /*
    @Test
    void shouldReturnInstantForGithubUrl() {
        String updated = "2026-03-10T14:39:32Z";
        when(githubClient.getRepository("owner", "repo")).thenReturn(new GitHubRepositoryResponse(updated));

        Instant result = metadataService.getLastUpdated(GIT_LINK);

        assertEquals(OffsetDateTime.parse(updated).toInstant(), result);
    }*/

    /*    @Test
    void shouldReturnInstantForStackoverflowUrl() {
        long epoch = 1710000000L;
        var item = new StackoverflowRepositoryResponse.StackoverflowQuestionResponse.QuestionItem(epoch);
        var response = new StackoverflowRepositoryResponse.StackoverflowQuestionResponse(List.of(item));

        when(stackoverflowClient.getQuestion(123L)).thenReturn(response);

        Instant result = metadataService.getLastUpdated(STACKOVERFLOW_LINK);

        assertEquals(Instant.ofEpochSecond(epoch), result);
    }*/

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
