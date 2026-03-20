package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.request.AddLinkRequest;
import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@AllArgsConstructor
public class TrackTagsHandler {

    private final TelegramBot telegramBot;
    private final UserStateStorage userStateStorage;
    private final BotMessageProperties botMessageProperties;
    private final ScrapperClient scrapperClient;

    public boolean supports(long chatId) {
        return userStateStorage.getState(chatId) == UserState.WAITING_TRACK_TAGS;
    }

    public boolean handle(long chatId, String text) {
        String pendingLink = userStateStorage.getPendingLink(chatId);
        if (pendingLink.equals("-")) {
            userStateStorage.clearState(chatId);
            return true;
        }

        List<String> tags = parseTags(text);

        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(pendingLink, tags, List.of()));
            telegramBot.execute(new SendMessage(chatId, botMessageProperties.getTrackSuccess()));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                telegramBot.execute(new SendMessage(chatId, botMessageProperties.getLinkAlreadyExists()));
            } else {
                telegramBot.execute(new SendMessage(chatId, "Ошибка при добавлении ссылки."));
            }
        } finally {
            userStateStorage.clearState(chatId);
        }

        return true;
    }

    private List<String> parseTags(String text) {
        if (text == null || text.isBlank() || text.equals("-")) {
            return List.of();
        }

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }
}
