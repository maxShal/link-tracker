package backend.academy.linktracker.bot.session;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.commands.Commands;
import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirstTimeCheckTest {

    private static final String WELCOME_MESSAGE = "WELCOME";

    @Mock
    private BotRepository botRepository;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotMessageProperties botMessageProperties;

    @InjectMocks
    private FirstTimeCheck firstTimeCheck;

    @Test
    void shouldReturnFalseForOldUser() {
        when(botRepository.isOld(1L)).thenReturn(true);

        boolean result = firstTimeCheck.newChat(1L, Commands.START);

        assertFalse(result);
        verifyNoInteractions(scrapperClient, telegramBot);
    }

    @Test
    void shouldHandleNewUserStart() {
        when(botRepository.isOld(1L)).thenReturn(false);
        when(botMessageProperties.getFirst()).thenReturn(WELCOME_MESSAGE);

        boolean result = firstTimeCheck.newChat(1L, Commands.START);

        assertTrue(result);
        verify(botRepository).save(1L);
        verify(scrapperClient).registerChat(1L);
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    void shouldReturnFalseForNewUserNonStartCommand() {
        when(botRepository.isOld(1L)).thenReturn(false);

        boolean result = firstTimeCheck.newChat(1L, "/help");

        assertFalse(result);
        verify(botRepository, never()).save(anyLong());
        verifyNoInteractions(scrapperClient, telegramBot);
    }
}
