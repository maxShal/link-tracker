package backend.academy.linktracker.bot.session;

import backend.academy.linktracker.bot.commands.Commands;
import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FirstTimeCheck {

    private final BotRepository botRepository;
    private final TelegramBot telegramBot;
    private final BotMessageProperties botMessageProperties;

    public boolean newChat(long chatId, String text) {
        if (botRepository.isOld(chatId)) return false;
        // infoLog("event", "new_chat", "New Chat");
        if (text.equals(Commands.START)) {
            botRepository.save(chatId);
            telegramBot.execute(new SendMessage(chatId, botMessageProperties.getFirst()));
            return true;
        }
        return false;
    }
}
