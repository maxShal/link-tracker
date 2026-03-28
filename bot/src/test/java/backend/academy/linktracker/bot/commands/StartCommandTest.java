package backend.academy.linktracker.bot.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.configuration.properties.BotMessageProperties;
import backend.academy.linktracker.bot.model.commands.StartCommand;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {

    private static final long CHAT_ID = 123L;
    private static final String START_COMMAND = "/start";
    private static final String START_MESSAGE = "Start";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotMessageProperties botMessageProperties;

    @InjectMocks
    private StartCommand startCommand;

    @Test
    void shouldSendStartMessage() {
        when(botMessageProperties.getStart()).thenReturn(START_MESSAGE);

        startCommand.handler(CHAT_ID, START_COMMAND);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        assertEquals(CHAT_ID, captor.getValue().getParameters().get("chat_id"));
        assertEquals(START_MESSAGE, captor.getValue().getParameters().get("text"));
    }

    @Test
    void shouldSupportStartCommand() {
        assertTrue(startCommand.supports("/start"));
        assertFalse(startCommand.supports("/help"));
        assertFalse(startCommand.supports("start"));
    }
}
