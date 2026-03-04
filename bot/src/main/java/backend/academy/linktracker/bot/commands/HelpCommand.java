package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class HelpCommand implements CommandHandler {

    private final TelegramBot bot;

    private final BotMessageProperties message;

    @Override
    public boolean supports(String cmd) {
        return cmd.equals(Commands.HELP);
    }

    @Override
    public void handler(long chatId, String command) {

        bot.execute(new SendMessage(chatId, message.getHelp()));
    }
}
