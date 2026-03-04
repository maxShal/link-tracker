package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleServiceTest {

    @Mock
    FirstTimeService  firstTimeService;
    @Mock
    CommandRoute commandRoute;

    @Mock
    Update update;

    @Mock
    TelegramBot bot;

    @Mock
    Message message;

    private HandleService handleService;

    @Mock
    Chat chat;

    @BeforeEach
    void setUp() {
        handleService = new HandleService(firstTimeService, commandRoute);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);
    }

    @Test
    void firstTimeServiceTest() {

        when(message.text()).thenReturn("/start");

        when(firstTimeService.newChat(123L,"/start")).thenReturn(true);

        handleService.handle(update);

        verify(firstTimeService).newChat(123L,"/start");
        verifyNoInteractions(commandRoute);
    }
}
