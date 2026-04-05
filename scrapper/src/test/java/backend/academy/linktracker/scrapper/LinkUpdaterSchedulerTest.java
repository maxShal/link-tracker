package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.MetadataService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    private BotMessageSender sender;

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
        LinkUpdateResponse response = new LinkUpdateResponse("title", "author", actual.toString(), "description");
        when(metadataService.getLastUpdated(link.url())).thenReturn(Optional.of(response));

        linkUpdaterScheduler.checkUpdates();

        verify(linksService).updateLastUpdated(1L, actual);
        verify(sender).sendMessageToBot(response, link);
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

        linkUpdaterScheduler.checkUpdates();

        verify(linksService).updateLastUpdated(1L, newDate);
        verify(sender).sendMessageToBot(response, link);
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
        verify(sender, never()).sendMessageToBot(any(), any());
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

        verify(sender, times(1)).sendMessageToBot(any(), any());
    }
}
