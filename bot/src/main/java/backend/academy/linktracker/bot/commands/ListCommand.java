package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.response.LinkResponse;
import backend.academy.linktracker.bot.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@AllArgsConstructor
public class ListCommand implements CommandHandler {

    private final TelegramBot telegramBot;

    private final ScrapperClient scrapperClient;

    private final BotMessageProperties botMessageProperties;

    @Override
    public boolean supports(String cmd) {
        return cmd.equals(Commands.LIST) || cmd.startsWith(Commands.LIST + " ");
    }

    @Override
    public void handler(long chatId, String command) {
        try {
            var links = scrapperClient.getLinks(chatId).links();
            var filtered = filterByTag(links, command);

            if (filtered.isEmpty()) {
                telegramBot.execute(new SendMessage(chatId, botMessageProperties.getList()));
                return;
            }
            telegramBot.execute(new SendMessage(chatId, formatLinks(filtered)));
        } catch (RestClientResponseException e) {
            telegramBot.execute(new SendMessage(chatId, "Ошибка получения списка ссылок."));
        }
    }

    private List<LinkResponse> filterByTag(List<LinkResponse> links, String command) {
        String[] parts = command.split("\\s+", 2);
        if (parts.length < 2) return links;
        String tag = parts[1].trim();
        return links.stream()
                .filter(link -> link.tags() != null && link.tags().contains(tag))
                .toList();
    }

    private String formatLinks(List<LinkResponse> links) {
        StringBuilder sb = new StringBuilder("Отслеживаемые ссылки:\n");
        for (LinkResponse link : links) {
            sb.append("- ").append(link.url());
            if (link.tags() != null && !link.tags().isEmpty()) {
                sb.append(" [").append(String.join(", ", link.tags())).append("]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
