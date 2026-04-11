package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import backend.academy.linktracker.scrapper.senders.HttpMessageSender;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.MetadataService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdaterSchedulerTest {

    private static final String LINK = "https://github.com/owner/repo";

    @Mock
    private HttpMessageSender sender;

    @Mock
    private SchedulerProperties properties;

    @Mock
    private LinksService linksService;

    @Mock
    private MetadataService metadataService;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private LinkUpdaterScheduler linkUpdaterScheduler;

    @BeforeEach
    void setUp() throws InterruptedException {
        when(properties.getPage()).thenReturn(0);
        when(properties.getSize()).thenReturn(10);
        when(properties.getThreads()).thenReturn(1);
        when(executorService.invokeAll(any())).thenAnswer(invoc -> {
            List<Callable<Void>> tasks = invoc.getArgument(0);

            List<Future<Void>> futures = new ArrayList<>();
            for (Callable<Void> task : tasks) {
                task.call();
                futures.add(CompletableFuture.completedFuture(null));
            }
            return futures;
        });
    }

    @Test
    void shouldSendUpdateWhenLastUpdatedIsNull() {
        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L, 2L), null);

        Instant actual = Instant.now();
        when(linksService.findAllForUpdateCheck(0, 10)).thenReturn(List.of(link));
        LinkUpdateResponse response = new LinkUpdateResponse("title", "author", actual.toString(), "description");
        when(metadataService.getLastUpdated(link.url())).thenReturn(Optional.of(response));
        LinkForSend send = new LinkForSend(
                link.id(),
                link.url(),
                link.tgChatIds(),
                response.title(),
                response.author(),
                response.createdAt(),
                response.description());

        linkUpdaterScheduler.checkUpdates();

        verify(linksService).updateLastUpdated(eq(1L), eq(actual));
        verify(sender).send(send);
    }

    @Test
    void shouldSendUpdateWhenActualDateIsAfterStoredDate() {
        Instant oldDate = Instant.parse("2026-03-10T10:00:00Z");
        Instant newDate = Instant.parse("2026-03-10T12:00:00Z");

        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), oldDate);
        when(linksService.findAllForUpdateCheck(properties.getPage(), properties.getSize()))
                .thenReturn(List.of(link));
        LinkUpdateResponse response = new LinkUpdateResponse("title", "author", newDate.toString(), "description");

        when(metadataService.getLastUpdated(link.url())).thenReturn(Optional.of(response));
        LinkForSend send = new LinkForSend(
                link.id(),
                link.url(),
                link.tgChatIds(),
                response.title(),
                response.author(),
                response.createdAt(),
                response.description());
        linkUpdaterScheduler.checkUpdates();

        verify(linksService).updateLastUpdated(1L, newDate);
        verify(sender).send(send);
    }

    @Test
    void shouldNotSendUpdateWhenDateDidNotChange() {
        Instant date = Instant.parse("2026-03-10T10:00:00Z");

        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), date);

        when(linksService.findAllForUpdateCheck(properties.getPage(), properties.getSize()))
                .thenReturn(List.of(link));
        LinkUpdateResponse response = new LinkUpdateResponse("title", "author", date.toString(), "description");

        when(metadataService.getLastUpdated(link.url())).thenReturn(Optional.of(response));
        linkUpdaterScheduler.checkUpdates();
        verify(linksService, never()).updateLastUpdated(anyLong(), any());
        verify(sender, never()).send(any());
    }

    @Test
    void shouldContinueWhenMetadataServiceThrows() {
        LinkForUpdateCheck first = new LinkForUpdateCheck(1L, "https://github.com/owner/repo1", List.of(1L), null);
        LinkForUpdateCheck second = new LinkForUpdateCheck(2L, "https://github.com/owner/repo2", List.of(2L), null);

        when(linksService.findAllForUpdateCheck(properties.getPage(), properties.getSize()))
                .thenReturn(List.of(first, second));
        LinkUpdateResponse response =
                new LinkUpdateResponse("title", "author", Instant.now().toString(), "description");

        when(metadataService.getLastUpdated(first.url())).thenThrow(new RuntimeException("Exception"));
        when(metadataService.getLastUpdated(second.url())).thenReturn(Optional.of(response));

        linkUpdaterScheduler.checkUpdates();

        verify(sender, times(1)).send(any());
    }

    @Test
    void shouldContinueProcessingWhenOneLinkFails() {
        LinkForUpdateCheck badLink = new LinkForUpdateCheck(1L, "https://github.com/bad/repo", List.of(1L), null);
        LinkForUpdateCheck goodLink = new LinkForUpdateCheck(2L, "https://github.com/good/repo", List.of(1L), null);

        when(properties.getPage()).thenReturn(0);
        when(properties.getSize()).thenReturn(10);

        when(linksService.findAllForUpdateCheck(0, 10)).thenReturn(List.of(badLink, goodLink));
        when(linksService.findAllForUpdateCheck(1, 10)).thenReturn(List.of());

        when(metadataService.getLastUpdated(badLink.url())).thenThrow(new RuntimeException("GitHub API unavailable"));

        LinkUpdateResponse goodResponse =
                new LinkUpdateResponse("New issue", "octocat", "2026-04-07T10:00:00Z", "Issue description");

        when(metadataService.getLastUpdated(goodLink.url())).thenReturn(Optional.of(goodResponse));
        LinkForSend send = new LinkForSend(
                goodLink.id(),
                goodLink.url(),
                goodLink.tgChatIds(),
                goodResponse.title(),
                goodResponse.author(),
                goodResponse.createdAt(),
                goodResponse.description());
        LinkForSend badSend = new LinkForSend(badLink.id(), badLink.url(), badLink.tgChatIds(), null, null, null, null);
        linkUpdaterScheduler.checkUpdates();

        verify(sender, times(1)).send(send);
        verify(linksService)
                .updateLastUpdated(
                        eq(2L), eq(OffsetDateTime.parse("2026-04-07T10:00:00Z").toInstant()));
        verify(sender, never()).send(badSend);
    }

    @Test
    void shouldProcessLinksInBatches() {
        LinkForUpdateCheck link1 = new LinkForUpdateCheck(1L, "https://github.com/owner/repo1", List.of(1L), null);
        LinkForUpdateCheck link2 = new LinkForUpdateCheck(2L, "https://github.com/owner/repo2", List.of(1L), null);

        when(properties.getPage()).thenReturn(0);
        when(properties.getSize()).thenReturn(1);

        when(linksService.findAllForUpdateCheck(0, 1)).thenReturn(List.of(link1));
        when(linksService.findAllForUpdateCheck(1, 1)).thenReturn(List.of(link2));
        when(linksService.findAllForUpdateCheck(2, 1)).thenReturn(List.of());

        LinkUpdateResponse response1 =
                new LinkUpdateResponse("title1", "author1", "2026-04-07T10:00:00Z", "description1");
        LinkUpdateResponse response2 =
                new LinkUpdateResponse("title2", "author2", "2026-04-07T11:00:00Z", "description2");

        when(metadataService.getLastUpdated(link1.url())).thenReturn(Optional.of(response1));
        when(metadataService.getLastUpdated(link2.url())).thenReturn(Optional.of(response2));

        linkUpdaterScheduler.checkUpdates();

        verify(linksService).findAllForUpdateCheck(0, 1);
        verify(linksService).findAllForUpdateCheck(1, 1);
        verify(linksService).findAllForUpdateCheck(2, 1);
        LinkForSend send1 = new LinkForSend(
                link1.id(),
                link1.url(),
                link1.tgChatIds(),
                response1.title(),
                response1.author(),
                response1.createdAt(),
                response1.description());
        LinkForSend send2 = new LinkForSend(
                link2.id(),
                link2.url(),
                link2.tgChatIds(),
                response2.title(),
                response2.author(),
                response2.createdAt(),
                response2.description());
        verify(sender).send(send1);
        verify(sender).send(send2);
    }

    @Test
    void shouldIsolateErrorsInsideBatch() {
        LinkForUpdateCheck link1 = new LinkForUpdateCheck(1L, "https://github.com/bad/repo", List.of(1L), null);
        LinkForUpdateCheck link2 = new LinkForUpdateCheck(2L, "https://github.com/good/repo", List.of(1L), null);
        LinkForUpdateCheck link3 = new LinkForUpdateCheck(3L, "https://github.com/good/repo2", List.of(1L), null);

        when(properties.getPage()).thenReturn(0);
        when(properties.getSize()).thenReturn(10);

        when(linksService.findAllForUpdateCheck(0, 10)).thenReturn(List.of(link1, link2, link3));
        when(linksService.findAllForUpdateCheck(1, 10)).thenReturn(List.of());

        when(metadataService.getLastUpdated(link1.url())).thenThrow(new RuntimeException("Некорректная ссылка"));

        LinkUpdateResponse response2 =
                new LinkUpdateResponse("title2", "author2", "2026-04-07T10:00:00Z", "description2");
        LinkUpdateResponse response3 =
                new LinkUpdateResponse("title3", "author3", "2026-04-07T11:00:00Z", "description3");

        when(metadataService.getLastUpdated(link2.url())).thenReturn(Optional.of(response2));
        when(metadataService.getLastUpdated(link3.url())).thenReturn(Optional.of(response3));
        LinkForSend send1 = new LinkForSend(link1.id(), link1.url(), link1.tgChatIds(), null, null, null, null);
        LinkForSend send2 = new LinkForSend(
                link2.id(),
                link2.url(),
                link2.tgChatIds(),
                response2.title(),
                response2.author(),
                response2.createdAt(),
                response2.description());
        LinkForSend send3 = new LinkForSend(
                link3.id(),
                link3.url(),
                link3.tgChatIds(),
                response3.title(),
                response3.author(),
                response3.createdAt(),
                response3.description());

        linkUpdaterScheduler.checkUpdates();

        verify(sender, never()).send(eq(send1));
        verify(sender).send(send2);
        verify(sender).send(send3);
    }
}
