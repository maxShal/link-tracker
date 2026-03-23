package backend.academy.linktracker.bot.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UntrackLinkHandlerTest {
    private static final String MESSAGE = "MESSAGE";

    private static final String LINK = "https://github.com/owner/repo";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private UserStateStorage userStateStorage;

    @Mock
    private BotMessageProperties botMessageProperties;

    @Mock
    private ScrapperClient scrapperClient;

    @InjectMocks
    private UntrackLinkHandler untrackLinkHandler;

    @Test
    void supportsShouldReturnTrueWhenStateIsWaitingUntrackLink() {
        when(userStateStorage.getState(1L)).thenReturn(UserState.WAITING_UNTRACK_LINK);

        assertTrue(untrackLinkHandler.supports(1L));
    }

    @Test
    void shouldSendInvalidLinkMessageForWrongLink() {
        when(botMessageProperties.getInvalidLink()).thenReturn(MESSAGE);

        untrackLinkHandler.handle(1L, "wrong_link");

        verifyNoInteractions(scrapperClient);
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    void shouldDeleteLinkAndSendSuccessMessage() {
        long chatId = 1L;
        when(botMessageProperties.getUntrackSuccess()).thenReturn(MESSAGE);

        untrackLinkHandler.handle(chatId, LINK);

        verify(scrapperClient).deleteLink(eq(chatId), any(RemoveLinkRequest.class));
        verify(userStateStorage).clearState(chatId);
        verify(telegramBot).execute(any(SendMessage.class));
    }
}
