package backend.academy.linktracker.bot.model.commands;

import backend.academy.linktracker.bot.configuration.properties.BotMessageProperties;
import backend.academy.linktracker.bot.handler.state.UserState;
import backend.academy.linktracker.bot.handler.state.UserStateStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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
