package backend.academy.linktracker.bot.session;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.commands.Commands;
import backend.academy.linktracker.bot.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FirstTimeCheck {

    // private final BotRepository botRepository;
    private final ScrapperClient scrapperClient;
    private final TelegramBot telegramBot;
    private final BotMessageProperties botMessageProperties;

    public boolean newChat(long chatId, String text) {
        if (scrapperClient.existChat(chatId)) return false;
        if (text.equals(Commands.START)) {
            // botRepository.save(chatId);
            scrapperClient.registerChat(chatId);
            telegramBot.execute(new SendMessage(chatId, botMessageProperties.getFirst()));
            return true;
        }
        return false;
    }
}
