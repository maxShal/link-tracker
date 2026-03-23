package backend.academy.linktracker.bot.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.request.AddLinkRequest;
import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackTagsHandlerTest {
    private static final String LINK = "https://github.com/owner/repo";

    private static final String MESSAGE = "MESSAGE";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private UserStateStorage userStateStorage;

    @Mock
    private BotMessageProperties botMessageProperties;

    @Mock
    private ScrapperClient scrapperClient;

    @InjectMocks
    private TrackTagsHandler trackTagsHandler;

    @Test
    void shouldReturnTrueWhenStateIsWaitingTrackTags() {
        when(userStateStorage.getState(1L)).thenReturn(UserState.WAITING_TRACK_TAGS);

        assertTrue(trackTagsHandler.supports(1L));
    }

    @Test
    void shouldContinueWithoutTagsWhenDashIsSent() {
        long chatId = 1L;

        when(userStateStorage.getPendingLink(chatId)).thenReturn(LINK);
        when(botMessageProperties.getTrackSuccess()).thenReturn(MESSAGE);

        trackTagsHandler.handle(chatId, "-");

        verify(userStateStorage).getPendingLink(chatId);
        verify(scrapperClient).addLink(eq(chatId), eq(new AddLinkRequest(LINK, List.of())));
        verify(userStateStorage).clearState(chatId);
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    void shouldAddLinkAndSendSuccessMessage() {
        long chatId = 1L;
        when(userStateStorage.getPendingLink(chatId)).thenReturn(LINK);
        when(botMessageProperties.getTrackSuccess()).thenReturn(MESSAGE);

        trackTagsHandler.handle(chatId, "work, study");

        verify(scrapperClient).addLink(eq(chatId), any(AddLinkRequest.class));
        verify(userStateStorage).clearState(chatId);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());
        assertTrue(captor.getValue().toWebhookResponse().contains(MESSAGE));
    }
}
