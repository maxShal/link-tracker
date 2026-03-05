package backend.academy.linktracker.bot.update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.commands.CommandRoute;
import backend.academy.linktracker.bot.session.FirstTimeCheck;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateHandlerTest {

    private static final long CHAT_ID = 123L;
    private static final String START_COMMAND = "/start";

    @Mock
    private FirstTimeCheck firstTimeCheck;

    @Mock
    private CommandRoute commandRoute;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @InjectMocks
    private UpdateHandler updateHandler;

    @BeforeEach
    void setUpdate() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
        when(message.text()).thenReturn(START_COMMAND);
    }

    @Test
    void shouldNotRouteWhenNewChat() {

        when(firstTimeCheck.newChat(CHAT_ID, START_COMMAND)).thenReturn(true);

        updateHandler.handle(update);

        verify(firstTimeCheck).newChat(CHAT_ID, START_COMMAND);
        verifyNoInteractions(commandRoute);
    }

    @Test
    void shouldRouteWhenNotFirstTime() {

        when(firstTimeCheck.newChat(CHAT_ID, START_COMMAND)).thenReturn(false);

        updateHandler.handle(update);

        verify(commandRoute).route(CHAT_ID, START_COMMAND);
    }
}
