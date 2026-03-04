package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FirstTimeService {

    private final BotRepository repository;
    private final TelegramBot bot;
    private final BotMessageProperties message;

    public boolean newChat(long chatId, String text)
    {
        if(repository.isOld(chatId)) return false;
        //infoLog("event", "new_chat", "New Chat");
        if ( text.equals(Commands.START)) {
            repository.save(chatId);
            bot.execute(new SendMessage(chatId, message.getFirst()));
            return true;
        }
        return false;
    }
}
