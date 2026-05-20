package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.model.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.TelegramBotService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "http" /*, matchIfMissing = true*/)
public class UpdatesController {

    private final TelegramBotService telegramBotService;

    @PostMapping("/updates")
    public ResponseEntity<Void> postUpdate(@Valid @RequestBody LinkUpdate update) {
        telegramBotService.sendUpdate(update);
        return ResponseEntity.ok().build();
    }
}
