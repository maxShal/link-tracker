package backend.academy.linktracker.bot.model.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandsInit {
    private final TelegramBot telegramBot;

    public BaseResponse comInit() {
        return telegramBot.execute(new SetMyCommands(
                new BotCommand(Commands.START, "Начало работы"),
                new BotCommand(Commands.HELP, "Список команд"),
                new BotCommand(Commands.TRACK, "Начать отслеживать ссылку"),
                new BotCommand(Commands.UNTRACK, "Перестать отслеживать ссылку"),
                new BotCommand(Commands.LIST, "список отслеживаемых ссылок с тегом"),
                new BotCommand(Commands.CANCEL, "Отмена")));
    }
}
