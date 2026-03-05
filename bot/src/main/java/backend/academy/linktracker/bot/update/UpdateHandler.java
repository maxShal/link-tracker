package backend.academy.linktracker.bot.update;

import backend.academy.linktracker.bot.commands.CommandRoute;
import backend.academy.linktracker.bot.session.FirstTimeCheck;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UpdateHandler {
    private final FirstTimeCheck firstTimeCheck;
    private final CommandRoute commandRoute;

    public void handle(Update update) {
        if (update.message() == null || update.message().text() == null) return;
        long chatId = update.message().chat().id();
        String text = update.message().text();

        if (firstTimeCheck.newChat(chatId, text)) {
            return;
        }
        commandRoute.route(chatId, text);
    }
}
