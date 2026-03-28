package backend.academy.linktracker.bot.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.configuration.properties.BotMessageProperties;
import backend.academy.linktracker.bot.handler.state.UserState;
import backend.academy.linktracker.bot.handler.state.UserStateStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackLinkHandlerTest {
    private static final String MESSAGE = "MESSAGE";

    private static final String LINK = "https://github.com/owner/repo";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private UserStateStorage userStateStorage;

    @Mock
    private BotMessageProperties botMessageProperties;

    @InjectMocks
    private TrackLinkHandler trackLinkHandler;

    @Test
    void shouldReturnTrueWhenStateIsWaitingTrackLink() {
        long chatId = 1L;
        when(userStateStorage.getState(chatId)).thenReturn(UserState.WAITING_TRACK_LINK);

        boolean result = trackLinkHandler.supports(chatId);

        assertTrue(result);
    }

    @Test
    void handleShouldSendInvalidLinkMessageForWrongLink() {
        long chatId = 1L;
        when(botMessageProperties.getInvalidLink()).thenReturn(MESSAGE);

        boolean result = trackLinkHandler.handle(chatId, "wrong_link");

        assertTrue(result);
        verify(userStateStorage, never()).setPendingLink(anyLong(), anyString());
        verify(userStateStorage, never()).setState(eq(chatId), eq(UserState.WAITING_TRACK_TAGS));

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());
        assertTrue(captor.getValue().toWebhookResponse().contains(MESSAGE));
    }

    @Test
    void shouldSavePendingLinkAndSwitchStateForValidLink() {
        long chatId = 2L;
        String url = LINK;
        when(botMessageProperties.getTrackTags()).thenReturn(MESSAGE);

        boolean result = trackLinkHandler.handle(chatId, url);

        assertTrue(result);
        verify(userStateStorage).setPendingLink(chatId, url);
        verify(userStateStorage).setState(chatId, UserState.WAITING_TRACK_TAGS);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());
        assertTrue(captor.getValue().toWebhookResponse().contains(MESSAGE));
    }
}
