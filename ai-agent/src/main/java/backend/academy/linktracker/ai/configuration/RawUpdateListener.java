package backend.academy.linktracker.ai.configuration;

import backend.academy.linktracker.ai.configuration.properties.KafkaProducerProperties;
import backend.academy.linktracker.ai.model.FilteredLinkUpdate;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.service.UpdateProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RawUpdateListener {

    private final KafkaTemplate<String, FilteredLinkUpdate> kafkaTemplate;
    private final KafkaProducerProperties kafkaProducerProperties;
    private final UpdateProcessingService processingService;

    @KafkaListener(topics = "${app.kafka-consumer.topic}", containerFactory = "defaultFactory")
    public void listen(RawLinkUpdate update) {
        try{
            log.atInfo()
                .addKeyValue("id", update.id())
                .log("Получил raw update: {}", update);

            processingService.process(update)
                .ifPresent(processed -> {
                    kafkaTemplate.send(kafkaProducerProperties.getTopic(), processed);
                    log.atInfo()
                        .addKeyValue("id", update.id())
                        .log("Отправил update: {}", processed);
                });
        }catch(Exception e){
            log.error("Ошибка обработки raw update: {}", update, e);
            throw e;
        }
    }
}
