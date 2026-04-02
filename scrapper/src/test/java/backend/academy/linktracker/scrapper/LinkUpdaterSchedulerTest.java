package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.MetadataService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdaterSchedulerTest {

    private static final String LINK = "https://github.com/owner/repo";

    @Mock
    private ILinksRepository linksRepository;

    @Mock
    private SchedulerProperties properties;

    @Mock
    private LinksService linksService;

    @Mock
    private MetadataService metadataService;

    @Mock
    private BotClient botClient;

    @InjectMocks
    private LinkUpdaterScheduler linkUpdaterScheduler;

    @Test
    void shouldSendUpdateWhenLastUpdatedIsNull() {
        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L, 2L), null);

        Instant actual = Instant.now();
        when(linksService.findAllForUpdateCheck(properties.getPage(), properties.getSize()))
                .thenReturn(List.of(link));
        when(metadataService.getLastUpdated(link.url())).thenReturn(actual);

        linkUpdaterScheduler.checkUpdates();

        verify(linksService).updateLastUpdated(1L, actual);
        verify(botClient).sendUpdate(any(LinkUpdateRequest.class));
    }

    @Test
    void shouldSendUpdateWhenActualDateIsAfterStoredDate() {
        Instant oldDate = Instant.parse("2026-03-10T10:00:00Z");
        Instant newDate = Instant.parse("2026-03-10T12:00:00Z");

        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), oldDate);

        when(linksService.findAllForUpdateCheck(properties.getPage(), properties.getSize()))
                .thenReturn(List.of(link));
        when(metadataService.getLastUpdated(link.url())).thenReturn(newDate);

        linkUpdaterScheduler.checkUpdates();

        verify(linksService).updateLastUpdated(1L, newDate);
        verify(botClient).sendUpdate(any(LinkUpdateRequest.class));
    }

    @Test
    void shouldNotSendUpdateWhenDateDidNotChange() {
        Instant date = Instant.parse("2026-03-10T10:00:00Z");

        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), date);

        when(linksService.findAllForUpdateCheck(properties.getPage(), properties.getSize()))
                .thenReturn(List.of(link));
        when(metadataService.getLastUpdated(link.url())).thenReturn(date);
        linkUpdaterScheduler.checkUpdates();
        verify(linksService, never()).updateLastUpdated(anyLong(), any());
        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void shouldContinueWhenMetadataServiceThrows() {
        LinkForUpdateCheck first = new LinkForUpdateCheck(1L, "https://github.com/owner/repo1", List.of(1L), null);
        LinkForUpdateCheck second = new LinkForUpdateCheck(2L, "https://github.com/owner/repo2", List.of(2L), null);

        when(linksService.findAllForUpdateCheck(properties.getPage(), properties.getSize()))
                .thenReturn(List.of(first, second));
        when(metadataService.getLastUpdated(first.url())).thenThrow(new RuntimeException("Exception"));
        when(metadataService.getLastUpdated(second.url())).thenReturn(Instant.now());

        linkUpdaterScheduler.checkUpdates();

        verify(botClient, times(1)).sendUpdate(any());
    }
}
