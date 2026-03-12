package backend.academy.linktracker.bot.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.state.UserStateStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelCommandTest {

    private static final String CANCEL_COMMAND = "/cancel";
    private static final String CANCEL_TEXT = "CANCEL_TEXT";
    private static final String START_COMMAND = "/start";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotMessageProperties botMessageProperties;

    @Mock
    private UserStateStorage userStateStorage;

    @InjectMocks
    private CancelCommand cancelCommand;

    @Test
    void shouldReturnTrueForCancel() {
        assertTrue(cancelCommand.supports(Commands.CANCEL));
        assertFalse(cancelCommand.supports(Commands.START));
    }

    @Test
    void shouldClearStateAndSendCancelMessage() {
        long chatId = 1L;
        when(botMessageProperties.getCancel()).thenReturn(CANCEL_TEXT);

        cancelCommand.handler(chatId, Commands.CANCEL);

        verify(userStateStorage).clearState(chatId);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());
        assertTrue(captor.getValue().toWebhookResponse().contains(CANCEL_TEXT));
    }
}
