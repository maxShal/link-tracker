package backend.academy.linktracker.scrapper.senders;

import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.util.Utils;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class MassageForSendMaker {
    public LinkUpdateRequest linkForSend(LinkForSend linkForSend) {
        String author = linkForSend.author();
        String tittle = linkForSend.title();
        String body = linkForSend.description();

        String updateTime = OffsetDateTime.parse(linkForSend.createdAt())
                .atZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));


        String description = """
                        Название: %s%n\
                        Время создания: %s%n\
                        Описание: %s%n\
                        """.formatted(tittle, updateTime, replaceHtml(makeShorter(body)));
        return new LinkUpdateRequest(
                linkForSend.linkId(), linkForSend.url(), description, author, linkForSend.tgChatIds());
    }

    private String makeShorter(String body) {
        if (body != null && body.length() > Utils.MAX_POST_LENGTH) {
            return body.substring(0, Utils.MAX_POST_LENGTH);
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
