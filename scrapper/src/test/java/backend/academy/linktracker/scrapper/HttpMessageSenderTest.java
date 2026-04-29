package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.senders.HttpMessageSender;
import backend.academy.linktracker.scrapper.senders.MassageForSendMaker;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpMessageSenderTest {
    @Mock
    private BotClient botClient;

    @Mock
    MassageForSendMaker massageForSendMaker;

    @InjectMocks
    private HttpMessageSender httpMessageSender;

    @Test
    void shouldSendShortMessage() {
        /*String longText = "a".repeat(250);*/

        LinkForSend linkForSend = new LinkForSend(
                1L, "https://github.com/owner/repo", "title", "author", "2026-04-07T10:00:00Z", "body", List.of(1L));
        LinkUpdateRequest request =
                new LinkUpdateRequest(1L, "https://github.com/owner/repo", "description", List.of(1L));

        when(massageForSendMaker.LinkForSend(linkForSend)).thenReturn(request);

        httpMessageSender.send(linkForSend);

        verify(botClient).sendUpdate(request);
    }
}
