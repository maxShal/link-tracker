package backend.academy.linktracker.bot.model.commands;

import backend.academy.linktracker.bot.configuration.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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
