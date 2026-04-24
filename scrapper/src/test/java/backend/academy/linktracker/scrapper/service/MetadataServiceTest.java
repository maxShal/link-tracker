package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackoverflowClient;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import backend.academy.linktracker.scrapper.service.strateges.GitHubStrategy;
import backend.academy.linktracker.scrapper.service.strateges.StackOverFlowStrategy;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private GitHubStrategy gitHubStrategy;

    @Mock
    private StackOverFlowStrategy stackOverFlowStrategy;

    @InjectMocks
    private MetadataService metadataService;

    @BeforeEach
    void setUp() {
        metadataService = new MetadataService(List.of(gitHubStrategy, stackOverFlowStrategy));
    }

    @Test
    void shouldReturnInstantForGithubUrl() {
        String updated = "2026-03-10T14:39:32Z";
        when(gitHubStrategy.patternCheck(GIT_LINK)).thenReturn(true);
        when(gitHubStrategy.getLastUpdated(GIT_LINK))
                .thenReturn(Optional.of(new LinkUpdateResponse("title", "author", updated, "description")));

        Instant result = OffsetDateTime.parse(
                        metadataService.getLastUpdated(GIT_LINK).get().createdAt())
                .toInstant();

        assertEquals(OffsetDateTime.parse(updated).toInstant(), result);
    }

    @Test
    void shouldReturnInstantForStackoverflowUrl() {
        long epoch = 1710000000L;
        long questionId = 123L;

        when(stackOverFlowStrategy.patternCheck(STACKOVERFLOW_LINK)).thenReturn(true);
        when(stackOverFlowStrategy.getLastUpdated(STACKOVERFLOW_LINK))
                .thenReturn(Optional.of(new LinkUpdateResponse(
                        "title", "author", Instant.ofEpochSecond(epoch).toString(), "description")));
        Instant result = OffsetDateTime.parse(
                        metadataService.getLastUpdated(STACKOVERFLOW_LINK).get().createdAt())
                .toInstant();
        assertEquals(Instant.ofEpochSecond(epoch), result);
    }

    @Test
    void shouldThrowWhenStackoverflowItemsEmpty() {
        when(stackOverFlowStrategy.patternCheck(STACKOVERFLOW_LINK)).thenReturn(true);
        when(stackOverFlowStrategy.getLastUpdated(STACKOVERFLOW_LINK)).thenReturn(Optional.empty());

        assertTrue(metadataService.getLastUpdated(STACKOVERFLOW_LINK).isEmpty());
    }
}
