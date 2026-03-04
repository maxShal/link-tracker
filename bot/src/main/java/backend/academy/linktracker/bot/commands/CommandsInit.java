package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CommandsInit {
    private final TelegramBot bot;

    public BaseResponse comInit() {
        return bot.execute(
                new SetMyCommands(new BotCommand("/start", "Начало работы"), new BotCommand("/help", "Список команд")));
    }
}
