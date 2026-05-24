package backend.academy.linktracker.scrapper.senders;

import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
public class KafkaMessageSender implements ISendUpdate {

    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;
    private final MassageForSendMaker massageForSendMaker;

    @Value("${app.message-send.topic}")
    private String topic;

    @Override
    public void send(LinkForSend linkForSend) {

        kafkaTemplate.send(topic, linkForSend.url(),massageForSendMaker.linkForSend(linkForSend));
    }
}
