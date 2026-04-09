package backend.academy.linktracker.scrapper.senders;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HttpMessageSender implements ISendUpdate {

    private final BotClient botClient;

    public void send(LinkUpdateResponse latestUpdate, LinkForUpdateCheck link) {

        String author = latestUpdate.author();
        String tittle = latestUpdate.title();
        String body = latestUpdate.description();

        String updateTime = OffsetDateTime.parse(latestUpdate.createdAt())
                .atZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        String description = """
                        %n\
                        Автор: %s%n\
                        Название: %s%n\
                        Время создания: %s%n\
                        Описание: %s%n\
                        """.formatted(author, tittle, updateTime, replaceHtml(makeShoter(body)));

        botClient.sendUpdate(new LinkUpdateRequest(link.id(), link.url(), description, link.tgChatIds()));
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
        return body.replaceAll("<[^>]*>", "");
    }
}
