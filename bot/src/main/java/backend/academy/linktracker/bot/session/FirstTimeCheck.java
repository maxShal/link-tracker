package backend.academy.linktracker.bot.session;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.configuration.properties.BotMessageProperties;
import backend.academy.linktracker.bot.model.commands.Commands;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FirstTimeCheck {

    private final ScrapperClient scrapperClient;
    private final TelegramBot telegramBot;
    private final BotMessageProperties botMessageProperties;

    public boolean newChat(long chatId, String text) {
        if (scrapperClient.existChat(chatId)) return false;
        if (text.equals(Commands.START)) {
            scrapperClient.registerChat(chatId);
            telegramBot.execute(new SendMessage(chatId, botMessageProperties.getFirst()));
            return true;
        }
        return false;
    }
}
