package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.integration.UpdateHandler;
import backend.academy.linktracker.bot.model.commands.CommandsInit;
import backend.academy.linktracker.bot.model.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TelegramBotService {

    private final TelegramBot telegramBot;

    private final UpdateHandler updateHandler;

    private final CommandsInit commandsInit;

    public void start() {
        infoLog("event", "bot_start", "Bot start");

        startInit();

        telegramBot.setUpdatesListener(
                updates -> {
                    for (Update update : updates) {
                        updateHandler.handle(update);
                    }
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                },
                e -> {
                    if (e.response() != null) {
                        errorLogWithCode(
                                "telegram_error",
                                e.response().errorCode(),
                                e.response().description(),
                                "Telegram Error");
                    } else {
                        errorLog("code", "telegram_network_error", e.getMessage());
                    }
                });
    }

    private void startInit() {
        var resp = commandsInit.comInit();

        if (!resp.isOk()) {
            errorLogWithCode("set_command_error", resp.errorCode(), resp.description(), "SetCommand Error");
        } else {
            infoLog("event", "set_command_ok", "SetCommand Ok");
        }
    }

    private void infoLog(String key, String value, String msg) {
        log.atInfo().addKeyValue(key, value).log(msg);
    }

    private void errorLog(String key, String value, String msg) {
        log.atError().addKeyValue(key, value).log(msg);
    }

    private void errorLogWithCode(String event, int code, String desc, String msg) {
        log.atError()
                .addKeyValue("event", event)
                .addKeyValue("code", code)
                .addKeyValue("desc", desc)
                .log(msg);
    }

    public void sendUpdate(LinkUpdate update) {
        for (Long chatId : update.tgChatIds()) {
            String desc = update.description();
            telegramBot.execute(new SendMessage(chatId, "Обновление по ссылке: " + update.url() + desc));
        }
    }
}
