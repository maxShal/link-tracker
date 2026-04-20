package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.model.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageConsumer {

    private final TelegramBotService service;

    @KafkaListener(containerFactory = "defaultFactory", topics = "${app.kafka.topic}")
    public void consume(ConsumerRecord<String, LinkUpdate> record) {
        service.sendUpdate(record.value());
    }
}
