package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.state.UserStateStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CancelCommand implements CommandHandler {

    private final TelegramBot telegramBot;
    private final BotMessageProperties botMessageProperties;
    private final UserStateStorage userStateStorage;

    @Override
    public boolean supports(String cmd) {
        return cmd.equals(Commands.CANCEL);
    }

    @Override
    public void handler(long chatId, String command) {
        userStateStorage.clearState(chatId);
        telegramBot.execute(new SendMessage(chatId, botMessageProperties.getCancel()));
    }
}
