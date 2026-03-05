package backend.academy.linktracker.bot.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandRouteTest {

    private static final long CHAT_ID = 123L;
    private static final String UNKNOWN_MESSAGE = "Неизвестная команда";

    @Mock
    private CommandHandler commandHandler;

    @Mock
    private BotMessageProperties botMessageProperties;

    @Mock
    private TelegramBot telegramBot;

    private CommandRoute commandRoute;

    @BeforeEach
    void setUp() {
        commandRoute = new CommandRoute(telegramBot, botMessageProperties, List.of(commandHandler));
    }

    @ParameterizedTest(name = "shouldHandleKnownCommand: {0}")
    @ValueSource(strings = {"/start", "/help", "/start123", "/help123"})
    void shouldCallHandlerForKnownCommand(String command) {
        when(commandHandler.supports(command)).thenReturn(true);

        commandRoute.route(CHAT_ID, command);

        verify(commandHandler).handler(CHAT_ID, command);
        verifyNoInteractions(telegramBot);
    }

    @ParameterizedTest(name = "shouldSendUnknownForUnrecognizedCommand: {0}")
    @ValueSource(strings = {"/start123", "/help123", "/randomСommand"})
    void shouldSendUnknownMessageForUnrecognizedCommand(String command) {
        when(commandHandler.supports(command)).thenReturn(false);
        when(botMessageProperties.getUnknown()).thenReturn(UNKNOWN_MESSAGE);

        commandRoute.route(CHAT_ID, command);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());
        SendMessage captured = captor.getValue();

        assertEquals(CHAT_ID, captured.getParameters().get("chat_id"));
        assertEquals(UNKNOWN_MESSAGE, captured.getParameters().get("text"));
    }

    @ParameterizedTest(name = "shouldIgnoreUserInput: {0}")
    @ValueSource(strings = {"start", "help", "привет", "random text", ""})
    void shouldDoNothingForPlainText(String text) {
        when(commandHandler.supports(text)).thenReturn(false);

        commandRoute.route(CHAT_ID, text);

        verifyNoInteractions(telegramBot);
    }
}
