package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.configuration.TelegramConfiguration;
import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@AllArgsConstructor
public class TelegramBotService {

    private final TelegramBot bot;

    private final Set<Long> users = ConcurrentHashMap.newKeySet();

    public void start(){
        log.atInfo() .addKeyValue("Info", "start" ) .log("Bot start");
        var resp =bot.execute(new SetMyCommands(
            new BotCommand("/start", "Начало работы"),
            new BotCommand("/help", "Список команд")
        ));

        if(!resp.isOk()){
            log.atError()
                .addKeyValue("SetCommand Code", resp.errorCode())
                .addKeyValue("SetCommand Description", resp.description())
                .log("SetCommand Error");
        }else{
            log.atError()
                .addKeyValue("Info", "Set start")
                .log("SetCommand Work");
        }

       bot.setUpdatesListener(
           new UpdatesListener() {
               @Override
               public int process(List<Update> updates) {
                   for (Update update : updates) {
                       handle(update);
                   }
                   return UpdatesListener.CONFIRMED_UPDATES_ALL;}
           },e -> {
                    if (e.response() != null) {
                        log.atError()
                            .addKeyValue("Telegram Code", e.response().errorCode())
                            .addKeyValue("Telegram Description", e.response().description())
                            .log("Telegram Error");

                    } else {
                        log.atError()
                            .addKeyValue("Error","network error")
                            .log("Telegram network error", e);
                    }
           });
    }


    void handle(Update update){
        if (update.message() == null || update.message().text() == null) return;
        long chatId = update.message().chat().id();
        String text = update.message().text();

        if (!users.contains(chatId)) {
            users.add(chatId);
            bot.execute(new SendMessage(
                chatId,
                "Добро пожаловать! Используйте /help, чтобы посмотреть доступные команды."));
            log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("command", text)
                .log("Send Start work Response");
        } else {
            switch (text) {
                case "/start":
                    bot.execute(new SendMessage(chatId, "Ответ на /start"));
                    log.atInfo()
                        .addKeyValue("chatId", chatId)
                        .addKeyValue("command", text)
                        .log("Send Response on /start");
                    break;
                case "/help":
                    bot.execute(new SendMessage(chatId, """
                                            На данный момент доступны команды\s
                                            /start - начало работы\s
                                            /help - список команд\
                                            """));
                    log.atInfo()
                        .addKeyValue("chatId", chatId)
                        .addKeyValue("command", text)
                        .log("Send Response on /help");
                    break;
                default:
                    if (text.startsWith("/")) {
                        log.atInfo()
                            .addKeyValue("chatId", chatId)
                            .addKeyValue("command", text)
                            .log("Invalid command");
                        bot.execute(new SendMessage(chatId, """
                                                Неизвестная команда.\s
                                                Воспользуйтесь /help\
                                                """));
                    }
            }
        }
    }
}
