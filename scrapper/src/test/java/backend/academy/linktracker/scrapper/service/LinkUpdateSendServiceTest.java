package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.senders.ISendUpdate;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdateSendServiceTest {

    private static final String LINK = "https://github.com/owner/repo";

    @Mock
    private LinkUpdateCheckService linkUpdateCheckService;

    @Mock
    private ISendUpdate sender;

    @InjectMocks
    private LinkUpdateSendService linkUpdateSendService;

    @Test
    void shouldSendWhenCheckServiceReturnedValue() {
        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), null);
        LinkForSend send = new LinkForSend(
                1L,
                LINK,
                "title",
                "author",
                Instant.parse("2026-04-20T10:00:00Z").toString(),
                "description",
                List.of(1L));

        when(linkUpdateCheckService.processLinkCheck(link)).thenReturn(send);

        linkUpdateSendService.processLinkSend(link);

        verify(sender).send(send);
    }

    @Test
    void shouldNotSendWhenCheckServiceReturnedEmpty() {
        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), null);

        when(linkUpdateCheckService.processLinkCheck(link)).thenReturn(null);

        linkUpdateSendService.processLinkSend(link);

        verify(sender, never()).send(any());
    }

    @Test
    void shouldNotThrowWhenCheckServiceFailed() {
        LinkForUpdateCheck link = new LinkForUpdateCheck(1L, LINK, List.of(1L), null);

        when(linkUpdateCheckService.processLinkCheck(link)).thenThrow(new RuntimeException("metadata failed"));

        assertDoesNotThrow(() -> linkUpdateSendService.processLinkSend(link));

        verify(sender, never()).send(any());
    }
}
