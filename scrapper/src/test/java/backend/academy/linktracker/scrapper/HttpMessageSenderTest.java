package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.senders.HttpMessageSender;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpMessageSenderTest {
    @Mock
    private BotClient botClient;

    @InjectMocks
    private HttpMessageSender httpMessageSender;

    @Test
    void shouldSendShortMessage() {
        String longText = "a".repeat(250);
        LinkForSend linkForSend = new LinkForSend(
                1L, "https://github.com/owner/repo", "title", "author", "2026-04-07T10:00:00Z", longText, List.of(1L));
        httpMessageSender.send(linkForSend);

        ArgumentCaptor<LinkUpdateRequest> captor = ArgumentCaptor.forClass(LinkUpdateRequest.class);

        verify(botClient).sendUpdate(captor.capture());

        String description = captor.getValue().description();
        assertTrue(description.contains("a".repeat(200)));
        assertFalse(description.contains("a".repeat(201)));
    }
}
