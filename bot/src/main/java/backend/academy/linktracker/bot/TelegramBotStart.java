package backend.academy.linktracker.bot;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app.telegram", name = "auto-start", havingValue = "true", matchIfMissing = true)
public class TelegramBotStart implements ApplicationRunner {
    private final TelegramBotService telegramBotService;

    @Override
    public void run(@NotNull ApplicationArguments args) throws Exception {
        telegramBotService.start();
    }
}
