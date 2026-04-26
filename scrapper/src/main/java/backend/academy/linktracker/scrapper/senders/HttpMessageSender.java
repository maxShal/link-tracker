package backend.academy.linktracker.scrapper.senders;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "http")
public class HttpMessageSender implements ISendUpdate {

    private final BotClient botClient;

    public void send(LinkForSend linkForSend) {

        String author = linkForSend.author();
        String tittle = linkForSend.title();
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
                        """.formatted(author, tittle, updateTime, replaceHtml(makeShorter(body)));

        botClient.sendUpdate(
                new LinkUpdateRequest(linkForSend.linkId(), linkForSend.url(), description, linkForSend.tgChatIds()));
    }

    private String makeShorter(String body) {
        if (body != null && body.length() > 200) {
            return body.substring(0, 200);
        }
        return body;
    }

    private String replaceHtml(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        return body.replaceAll("<[^>]*>", "");
    }
}
