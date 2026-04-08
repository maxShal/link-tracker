package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BotMessageSenderTest {
    @Mock
    private BotClient botClient;

    @InjectMocks
    private BotMessageSender botMessageSender;

    @Test
    void shouldSendShortMessage() {
        String longText = "a".repeat(250);

        LinkUpdateResponse linkUpdateResponse =
                new LinkUpdateResponse("title", "author", "2026-04-07T10:00:00Z", longText);

        LinkForUpdateCheck linkForUpdateCheck =
                new LinkForUpdateCheck(1L, "https://github.com/owner/repo", List.of(1L), null);

        botMessageSender.sendMessageToBot(linkUpdateResponse, linkForUpdateCheck);

        ArgumentCaptor<LinkUpdateRequest> captor = ArgumentCaptor.forClass(LinkUpdateRequest.class);

        verify(botClient).sendUpdate(captor.capture());

        String description = captor.getValue().description();
        assertTrue(description.contains("a".repeat(200)));
        assertFalse(description.contains("a".repeat(201)));
    }
}
