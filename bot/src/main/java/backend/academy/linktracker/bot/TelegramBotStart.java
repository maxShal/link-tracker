package backend.academy.linktracker.bot;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TelegramBotStart implements ApplicationRunner {
    private final TelegramBotService telegramBotService;

    @Override
    public void run(@NotNull ApplicationArguments args) throws Exception {
        telegramBotService.start();
    }
}
