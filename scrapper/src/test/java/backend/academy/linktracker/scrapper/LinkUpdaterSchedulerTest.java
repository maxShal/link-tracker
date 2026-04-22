package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.service.LinkUpdateSendService;
import backend.academy.linktracker.scrapper.service.LinksService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    private static final String LINK1 = "https://github.com/owner/repo1";
    private static final String LINK2 = "https://github.com/owner/repo2";

    @Mock
    private LinksService linksService;

    @Mock
    private LinkUpdateSendService linkUpdateSendService;

    @Mock
    private SchedulerProperties properties;

    @Mock
    private ExecutorService linkUpdateExecutor;

    @InjectMocks
    private LinkUpdaterScheduler linkUpdaterScheduler;

    @BeforeEach
    void setUp() throws Exception {
        when(properties.getPage()).thenReturn(0);
        when(properties.getSize()).thenReturn(10);
        when(properties.getThreads()).thenReturn(1);

        when(linkUpdateExecutor.invokeAll(any())).thenAnswer(invocation -> {
            List<Callable<Void>> tasks = invocation.getArgument(0);
            List<Future<Void>> futures = new ArrayList<>();

            for (Callable<Void> task : tasks) {
                task.call();
                futures.add(CompletableFuture.completedFuture(null));
            }

            return futures;
        });
    }

    @Test
    void shouldProcessLinksFromSinglePage() throws Exception {
        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK1, List.of(1L), null);

        when(linksService.findAllForUpdateCheck(0, 10)).thenReturn(List.of(link));
        when(linksService.findAllForUpdateCheck(1, 10)).thenReturn(List.of());

        linkUpdaterScheduler.checkUpdates();

        verify(linkUpdateSendService).processLinkSend(link);
    }

    @Test
    void shouldProcessLinksInBatches() throws Exception {
        LinkForUpdateCheck link1 = new LinkForUpdateCheck(1L, LINK1, List.of(1L), null);
        LinkForUpdateCheck link2 = new LinkForUpdateCheck(2L, LINK2, List.of(2L), Instant.now());

        when(linksService.findAllForUpdateCheck(0, 10)).thenReturn(List.of(link1, link2));
        when(linksService.findAllForUpdateCheck(1, 10)).thenReturn(List.of());

        linkUpdaterScheduler.checkUpdates();

        verify(linkUpdateSendService).processLinkSend(link1);
        verify(linkUpdateSendService).processLinkSend(link2);
    }

    @Test
    void shouldRequestNextPage() throws Exception {
        LinkForUpdateCheck link1 = new LinkForUpdateCheck(1L, LINK1, List.of(1L), null);
        LinkForUpdateCheck link2 = new LinkForUpdateCheck(2L, LINK2, List.of(2L), null);

        when(linksService.findAllForUpdateCheck(0, 10)).thenReturn(List.of(link1));
        when(linksService.findAllForUpdateCheck(1, 10)).thenReturn(List.of(link2));
        when(linksService.findAllForUpdateCheck(2, 10)).thenReturn(List.of());

        linkUpdaterScheduler.checkUpdates();

        verify(linksService).findAllForUpdateCheck(0, 10);
        verify(linksService).findAllForUpdateCheck(1, 10);
        verify(linksService).findAllForUpdateCheck(2, 10);

        verify(linkUpdateSendService).processLinkSend(link1);
        verify(linkUpdateSendService).processLinkSend(link2);
    }

    @Test
    void shouldContinueWhenOneTaskFails() throws Exception {
        LinkForUpdateCheck badLink = new LinkForUpdateCheck(1L, LINK1, List.of(1L), null);
        LinkForUpdateCheck goodLink = new LinkForUpdateCheck(2L, LINK2, List.of(2L), null);

        when(properties.getThreads()).thenReturn(2);

        when(linksService.findAllForUpdateCheck(0, 10)).thenReturn(List.of(badLink, goodLink));
        when(linksService.findAllForUpdateCheck(1, 10)).thenReturn(List.of());

        when(linkUpdateExecutor.invokeAll(anyList())).thenAnswer(invocation -> {
            List<Callable<Void>> tasks = invocation.getArgument(0);
            List<Future<Void>> futures = new ArrayList<>();

            boolean first = true;
            for (Callable<Void> task : tasks) {
                if (first) {
                    first = false;
                    futures.add(CompletableFuture.failedFuture(new RuntimeException("boom")));
                } else {
                    task.call();
                    futures.add(CompletableFuture.completedFuture(null));
                }
            }
            return futures;
        });

        assertDoesNotThrow(() -> linkUpdaterScheduler.checkUpdates());

        verify(linkUpdateSendService).processLinkSend(goodLink);
    }
}
