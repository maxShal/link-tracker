package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@AllArgsConstructor
public class CommandRoute
{
    private final TelegramBot bot;

    private final BotMessageProperties messageProp;

    private final List<CommandHandler> commandHandlers;

    public void route(long chatId, String message)
    {
        for(var commandHandler : commandHandlers)
        {
            if(commandHandler.supports(message))
            {
                commandHandler.handler(chatId, message);
                return;
            }
        }

        if (message.startsWith("/")) {
            bot.execute(new SendMessage(chatId, messageProp.getUnknown()));
        }
    }
}
