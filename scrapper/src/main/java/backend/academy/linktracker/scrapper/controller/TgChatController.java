package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.service.TgChatService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/tg-chat")
public class TgChatController {

    private final TgChatService tgChatService;

    @PostMapping("/{id}")
    public ResponseEntity<@NotNull Void> addChat(@PathVariable Long id) {
        tgChatService.addChat(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<@NotNull Void> removeChat(@PathVariable Long id) {
        tgChatService.removeChat(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NotNull Boolean> existChat(@PathVariable Long id) {
        boolean exist = tgChatService.existsChat(id);
        return ResponseEntity.ok(exist);
    }
}
