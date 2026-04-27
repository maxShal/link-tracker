package backend.academy.linktracker.scrapper.senders;

import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import backend.academy.linktracker.scrapper.util.Utils;
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

    @Value("${app.message-send.topic}")
    private String topic;

    @Override
    public void send(LinkForSend linkForSend) {

        String author = linkForSend.author();
        String title = linkForSend.title();
        String body = linkForSend.description();

        String updateTime = OffsetDateTime.parse(linkForSend.createdAt())
                .atZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        String description = """
                        %n\
                        Автор: %s%n\
                        Название: %s%n\
                        Время создания: %s%n\
                        Описание: %s%n\
                        """.formatted(author, title, updateTime, replaceHtml(makeShoter(body)));

        kafkaTemplate.send(
                topic,
                linkForSend.url(),
                new LinkUpdateRequest(linkForSend.linkId(), linkForSend.url(), description, linkForSend.tgChatIds()));
    }

    private String makeShoter(String body) {
        if (body != null && body.length() > 200) {
            return body.substring(0, 200);
        }
        return body;
    }

    private String replaceHtml(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        return body.replaceAll(Utils.HTML_REGEX, "");
    }
}
