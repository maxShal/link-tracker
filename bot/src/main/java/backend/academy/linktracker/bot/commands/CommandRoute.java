package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CommandRoute {
    private final TelegramBot telegramBot;

    private final BotMessageProperties botMessageProperties;

    private final List<CommandHandler> commandHandlers;

    public void route(long chatId, String message) {
        for (var commandHandler : commandHandlers) {
            if (commandHandler.supports(message)) {
                commandHandler.handler(chatId, message);
                return;
            }
        }

        if (message.startsWith("/")) {
            telegramBot.execute(new SendMessage(chatId, botMessageProperties.getUnknown()));
        }
    }
}
