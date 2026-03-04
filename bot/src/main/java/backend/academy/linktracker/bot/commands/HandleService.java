package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class HandleService {
    private final FirstTimeService firstTimeService;
    private final CommandRoute commandRoute;

    public void handle(Update update) {
        if (update.message() == null || update.message().text() == null) return;
        long chatId = update.message().chat().id();
        String text = update.message().text();

        if (firstTimeService.newChat(chatId, text)) {
            return;
        }
        commandRoute.route(chatId, text);
    }
}
