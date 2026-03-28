package backend.academy.linktracker.bot.model.commands;

import backend.academy.linktracker.bot.configuration.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartCommand implements CommandHandler {

    private final TelegramBot telegramBot;
    private final BotMessageProperties botMessageProperties;

    @Override
    public boolean supports(String cmd) {
        return cmd.equals(Commands.START);
    }

    @Override
    public void handler(long chatId, String command) {
        telegramBot.execute(new SendMessage(chatId, botMessageProperties.getStart()));
    }
}
