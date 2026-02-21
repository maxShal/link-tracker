package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TelegramBotServiceTest {

    // Объявление полей + создание mock.
    TelegramBot bot;
    TelegramBotService telegramBotService;

    Update update;
    Message message;
    Chat chat;

    @BeforeEach
    void setUp() {
        bot = mock(TelegramBot.class);
        telegramBotService = new TelegramBotService(bot);

        update = mock(Update.class);
        message = mock(Message.class);
        chat = mock(Chat.class);
    }

    // Ответ на первое сообщение
    @Test
    void welcomeCommand() {

        // Задаём параметры update сообщения
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("first message");
        when(message.chat().id()).thenReturn(1L);

        // Вызываем метод бота
        telegramBotService.handle(update);

        // Извлекаем первый аргумент
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        // Запоминаем вызов метода
        verify(bot).execute(captor.capture());
        SendMessage sendMessage = captor.getValue();
        String actualText = sendMessage.getText();
        assertEquals("Добро пожаловать! Используйте /help, чтобы посмотреть доступные команды.", actualText);
    }

    @Test
    void helpCommand() {

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/help");
        when(message.chat().id()).thenReturn(1L);

        telegramBotService.handle(update);
        reset(bot);

        telegramBotService.handle(update);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        verify(bot).execute(captor.capture());
        SendMessage sendMessage = captor.getValue();
        String actualText = sendMessage.getText();
        assertEquals("""
                                            На данный момент доступны команды\s
                                            /start - начало работы\s
                                            /help - список команд\
                                            """, actualText);
    }

    @Test
    void startCommand() {

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/start");
        when(message.chat().id()).thenReturn(1L);

        telegramBotService.handle(update);
        reset(bot);

        telegramBotService.handle(update);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        verify(bot).execute(captor.capture());
        SendMessage sendMessage = captor.getValue();
        String actualText = sendMessage.getText();
        assertEquals("Ответ на /start", actualText);
    }

    @Test
    void Command() {

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/Invalid");
        when(message.chat().id()).thenReturn(1L);

        telegramBotService.handle(update);
        reset(bot);

        telegramBotService.handle(update);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        verify(bot).execute(captor.capture());
        SendMessage sendMessage = captor.getValue();
        String actualText = sendMessage.getText();
        assertEquals("""
                                                Неизвестная команда.\s
                                                Воспользуйтесь /help\
                                                """, actualText);
    }
}
