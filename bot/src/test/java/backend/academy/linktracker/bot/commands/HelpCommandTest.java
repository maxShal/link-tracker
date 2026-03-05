package backend.academy.linktracker.bot.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelpCommandTest {

    private static final long CHAT_ID = 123L;
    private static final String HELP_COMMAND = "/help";
    private static final String HELP_MESSAGE = "Help";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotMessageProperties botMessageProperties;

    @InjectMocks
    private HelpCommand helpCommand;

    @Test
    void shouldSendHelpMessage() {
        when(botMessageProperties.getHelp()).thenReturn(HELP_MESSAGE);

        helpCommand.handler(CHAT_ID, HELP_COMMAND);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        assertEquals(CHAT_ID, captor.getValue().getParameters().get("chat_id"));
        assertEquals(HELP_MESSAGE, captor.getValue().getParameters().get("text"));
    }

    @Test
    void shouldSupportHelpCommand() {
        assertTrue(helpCommand.supports("/help"));
        assertFalse(helpCommand.supports("/start"));
        assertFalse(helpCommand.supports("help"));
    }
}
