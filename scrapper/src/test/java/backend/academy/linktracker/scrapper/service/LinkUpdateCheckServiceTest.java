package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdateCheckServiceTest {

    private static final String LINK = "https://github.com/owner/repo";

    @Mock
    private LinksService linksService;

    @Mock
    private MetadataService metadataService;

    @InjectMocks
    private LinkUpdateCheckService linkUpdateCheckService;

    @Test
    void shouldReturnLinkForSendWhenLastUpdatedIsNull() {
        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L, 2L), null);
        Instant actual = Instant.parse("2026-04-20T10:00:00Z");

        LinkUpdateResponse response = new LinkUpdateResponse("title", "author", actual.toString(), "description");

        when(metadataService.getLastUpdated(link.url())).thenReturn(Optional.of(response));

        LinkForSend result = linkUpdateCheckService.processLinkCheck(link);

        assertEquals(link.id(), result.linkId());
        assertEquals(link.url(), result.url());
        assertEquals(actual.toString(), result.createdAt());

        verify(linksService).updateLastUpdated(1L, actual);
    }

    @Test
    void shouldReturnLinkForSendWhenActualDateIsAfterStoredDate() {
        Instant oldDate = Instant.parse("2026-03-10T10:00:00Z");
        Instant newDate = Instant.parse("2026-03-10T12:00:00Z");

        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), oldDate);
        LinkUpdateResponse response = new LinkUpdateResponse("title", "author", newDate.toString(), "description");

        when(metadataService.getLastUpdated(link.url())).thenReturn(Optional.of(response));

        LinkForSend result = linkUpdateCheckService.processLinkCheck(link);

        assertEquals(newDate.toString(), result.createdAt());

        verify(linksService).updateLastUpdated(1L, newDate);
    }

    @Test
    void shouldReturnEmptyWhenDateDidNotChange() {
        Instant date = Instant.parse("2026-03-10T10:00:00Z");

        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), date);
        LinkUpdateResponse response = new LinkUpdateResponse("title", "author", date.toString(), "description");

        when(metadataService.getLastUpdated(link.url())).thenReturn(Optional.of(response));

        linkUpdateCheckService.processLinkCheck(link);

        verify(linksService, never()).updateLastUpdated(anyLong(), any());
    }

    @Test
    void shouldReturnEmptyWhenMetadataServiceReturnedEmpty() {
        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), null);

        when(metadataService.getLastUpdated(link.url())).thenReturn(Optional.empty());

        assertEquals(null, linkUpdateCheckService.processLinkCheck(link));

        verify(linksService, never()).updateLastUpdated(anyLong(), any());
    }
}
