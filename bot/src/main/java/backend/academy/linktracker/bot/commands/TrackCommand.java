package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TrackCommand implements CommandHandler {

    private final TelegramBot telegramBot;
    private final BotMessageProperties botMessageProperties;
    private final UserStateStorage userStateStorage;

    @Override
    public boolean supports(String cmd) {
        return cmd.equals(Commands.TRACK);
    }

    @Override
    public void handler(long chatId, String command) {
        userStateStorage.setState(chatId, UserState.WAITING_TRACK_LINK);
        telegramBot.execute(new SendMessage(chatId, botMessageProperties.getTrackLink()));
    }
}
