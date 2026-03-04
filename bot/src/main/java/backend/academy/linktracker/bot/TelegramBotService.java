package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.commands.CommandsInit;
import backend.academy.linktracker.bot.commands.HandleService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class TelegramBotService {

    private final TelegramBot bot;

    private final HandleService handleService;

    private final CommandsInit commandsInit;

    // private final ScrapperClient scrapperClient;

    public void start() {
        infoLog("event", "bot_start", "Bot start");

        startInit();

        bot.setUpdatesListener(
                updates -> {
                    for (Update update : updates) {
                        handleService.handle(update);
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
}
