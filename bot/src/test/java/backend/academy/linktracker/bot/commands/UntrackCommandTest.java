package backend.academy.linktracker.bot.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.configuration.properties.BotMessageProperties;
import backend.academy.linktracker.bot.handler.state.UserState;
import backend.academy.linktracker.bot.handler.state.UserStateStorage;
import backend.academy.linktracker.bot.model.commands.Commands;
import backend.academy.linktracker.bot.model.commands.UntrackCommand;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UntrackCommandTest {
    private static final String UNTRACK_MESSAGE = "UNTRACK_MESSAGE";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotMessageProperties botMessageProperties;

    @Mock
    private UserStateStorage userStateStorage;

    @InjectMocks
    private UntrackCommand untrackCommand;

    @Test
    void supportsShouldReturnTrueForUntrack() {
        assertTrue(untrackCommand.supports(Commands.UNTRACK));
    }

    @Test
    void handlerShouldSetWaitingUntrackStateAndSendMessage() {
        long chatId = 1L;
        when(botMessageProperties.getUntrackLink()).thenReturn(UNTRACK_MESSAGE);

        untrackCommand.handler(chatId, Commands.UNTRACK);

        verify(userStateStorage).setState(chatId, UserState.WAITING_UNTRACK_LINK);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());
        assertTrue(captor.getValue().toWebhookResponse().contains(UNTRACK_MESSAGE));
    }
}
