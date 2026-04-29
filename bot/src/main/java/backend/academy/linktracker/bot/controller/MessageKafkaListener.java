package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.exception.LinkUpdateException;
import backend.academy.linktracker.bot.model.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.TelegramBotService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka", matchIfMissing = true)
public class MessageKafkaListener {
    private final TelegramBotService telegramBotService;
    private final Validator validator;

    @KafkaListener(topics = "${app.kafka.topic}", containerFactory = "defaultFactory")
    public void listen(LinkUpdate linkUpdate) {
        validate(linkUpdate);
        log.atInfo()
                .addKeyValue("url", linkUpdate.url())
                .addKeyValue("chatIds", linkUpdate.tgChatIds())
                .log("Получил сообщение от Kafka");
        telegramBotService.sendUpdate(linkUpdate);
    }

    private void validate(LinkUpdate linkUpdate) {
        var violations = validator.validate(linkUpdate);

        if (!violations.isEmpty()) {
            throw new LinkUpdateException("Некорректная ссылка: " + violations);
        }
    }
}
