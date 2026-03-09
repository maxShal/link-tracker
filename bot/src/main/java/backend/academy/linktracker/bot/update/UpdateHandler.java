package backend.academy.linktracker.bot.update;

import backend.academy.linktracker.bot.commands.CommandRoute;
import backend.academy.linktracker.bot.handler.TrackLinkHandler;
import backend.academy.linktracker.bot.handler.TrackTagsHandler;
import backend.academy.linktracker.bot.handler.UntrackLinkHandler;
import backend.academy.linktracker.bot.session.FirstTimeCheck;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateStorage;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UpdateHandler {
    private final FirstTimeCheck firstTimeCheck;
    private final UserStateStorage userStateStorage;
    private final CommandRoute commandRoute;
    private final TrackTagsHandler trackTagsHandler;
    private final TrackLinkHandler trackLinkHandler;
    private final UntrackLinkHandler untrackLinkHandler;

    public void handle(Update update) {
        if (update.message() == null || update.message().text() == null) return;
        long chatId = update.message().chat().id();
        String text = update.message().text();

        if (firstTimeCheck.newChat(chatId, text)) {
            return;
        }
        if (userStateStorage.getState(chatId) != UserState.IDLE && text.startsWith("/")) {
            userStateStorage.clearState(chatId);
        }
        if (!text.startsWith("/")) {
            if (trackLinkHandler.supports(chatId)) {
                trackLinkHandler.handle(chatId, text);
                return;
            }

            if (trackTagsHandler.supports(chatId)) {
                trackTagsHandler.handle(chatId, text);
                return;
            }
            if (untrackLinkHandler.supports(chatId)) {
                untrackLinkHandler.handle(chatId, text);
                return;
            }
        }
        commandRoute.route(chatId, text);
    }
}
