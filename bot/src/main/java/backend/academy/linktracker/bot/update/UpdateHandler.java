package backend.academy.linktracker.bot.update;

import backend.academy.linktracker.bot.commands.CommandRoute;
import backend.academy.linktracker.bot.handler.TrackRoute;
import backend.academy.linktracker.bot.session.FirstTimeCheck;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateStorage;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateHandler {
    private final FirstTimeCheck firstTimeCheck;
    private final UserStateStorage userStateStorage;
    private final CommandRoute commandRoute;
    private final TrackRoute trackRoute;

    public void handle(Update update) {
        if (update.message() == null || update.message().text() == null) return;
        long chatId = update.message().chat().id();
        String text = update.message().text();

        if (firstTimeCheck.newChat(chatId, text)) {
            return;
        }
        clearStateAfterIncorrectMessage(chatId, text);

        trackRoute.route(chatId, text);
        commandRoute.route(chatId, text);
    }

    private void clearStateAfterIncorrectMessage(long chatId, String text) {
        if (userStateStorage.getState(chatId) != UserState.IDLE && text.startsWith("/")) {
            userStateStorage.clearState(chatId);
        }
    }
}
