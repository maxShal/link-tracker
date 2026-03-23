package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateStorage;
import backend.academy.linktracker.bot.utils.Utils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TrackLinkHandler implements ITrackHandler {

    private final TelegramBot telegramBot;
    private final UserStateStorage userStateStorage;
    private final BotMessageProperties botMessageProperties;

    @Override
    public boolean supports(long chatId) {
        return userStateStorage.getState(chatId) == UserState.WAITING_TRACK_LINK;
    }

    @Override
    public boolean handle(long chatId, String text) {
        if (!isValidLink(text)) {
            telegramBot.execute(new SendMessage(chatId, botMessageProperties.getInvalidLink()));
            return true;
        }

        userStateStorage.setPendingLink(chatId, text);
        userStateStorage.setState(chatId, UserState.WAITING_TRACK_TAGS);
        telegramBot.execute(new SendMessage(chatId, botMessageProperties.getTrackTags()));
        return true;
    }

    private boolean isValidLink(String text) {
        return text != null && (text.startsWith(Utils.HTTP) || text.startsWith(Utils.HTTPS));
    }
}
