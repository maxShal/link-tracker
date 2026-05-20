package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.model.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
public class KafkaMessageConsumer {

    private final TelegramBotService telegramBotService;

    @KafkaListener(containerFactory = "defaultFactory", topics = "${app.kafka.topic}")
    public void consume(ConsumerRecord<String, LinkUpdate> record) {
        telegramBotService.sendUpdate(record.value());
    }
}
