package backend.academy.linktracker.bot.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.response.LinkResponse;
import backend.academy.linktracker.bot.dto.response.ListLinksResponse;
import backend.academy.linktracker.bot.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListCommandTest {
    private static final String LINK = "https://github.com/owner/repo";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private BotMessageProperties botMessageProperties;

    @InjectMocks
    private ListCommand listCommand;

    @Test
    void shouldReturnTrueForList() {
        assertTrue(listCommand.supports(Commands.LIST));
        assertTrue(listCommand.supports(Commands.LIST + " work"));
    }

    @Test
    void shouldSendEmptyListMessageWhenNoLinks() {
        long chatId = 1L;
        when(botMessageProperties.getList()).thenReturn("EMPTY_LIST");
        when(scrapperClient.getLinks(chatId)).thenReturn(new ListLinksResponse(List.of(), 0));

        listCommand.handler(chatId, Commands.LIST);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());
        assertTrue(captor.getValue().toWebhookResponse().contains("EMPTY_LIST"));
    }

    @Test
    void shouldSendLinks() {
        long chatId = 1L;
        LinkResponse link = new LinkResponse(1L, LINK, List.of("work"), List.of());
        when(scrapperClient.getLinks(chatId)).thenReturn(new ListLinksResponse(List.of(link), 1));

        listCommand.handler(chatId, "/list");

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());
        String url = captor.getValue().toWebhookResponse();
        assertTrue(url.contains(LINK));
    }

    @Test
    void shouldFilterByTag() {
        long chatId = 1L;
        LinkResponse first = new LinkResponse(1L, "https://github.com/owner/repo1", List.of("work"), List.of());
        LinkResponse second = new LinkResponse(2L, "https://github.com/owner/repo2", List.of("study"), List.of());
        when(scrapperClient.getLinks(chatId)).thenReturn(new ListLinksResponse(List.of(first, second), 2));

        listCommand.handler(chatId, "/list work");

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());
        String url = captor.getValue().toWebhookResponse();
        assertTrue(url.contains("repo1"));
    }
}
