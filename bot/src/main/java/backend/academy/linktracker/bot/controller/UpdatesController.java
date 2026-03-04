package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.TelegramBotService;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UpdatesController {

    private TelegramBotService telegramBotService;

    @PostMapping("/updates")
    public ResponseEntity<Void> postUpdate(@Valid @RequestBody LinkUpdate update)
    {
        //telegramBotService.sendUpdate(update);
        return ResponseEntity.ok().build();
    }
}
