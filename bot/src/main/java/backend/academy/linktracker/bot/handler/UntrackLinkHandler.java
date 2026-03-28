package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.configuration.properties.BotMessageProperties;
import backend.academy.linktracker.bot.handler.state.UserState;
import backend.academy.linktracker.bot.handler.state.UserStateStorage;
import backend.academy.linktracker.bot.model.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.util.Utils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@AllArgsConstructor
public class UntrackLinkHandler implements ITrackHandler {

    private final TelegramBot telegramBot;
    private final UserStateStorage userStateStorage;
    private final BotMessageProperties botMessageProperties;
    private final ScrapperClient scrapperClient;

    @Override
    public boolean supports(long chatId) {
        return userStateStorage.getState(chatId) == UserState.WAITING_UNTRACK_LINK;
    }

    private boolean isValidLink(String text) {
        return text != null && (text.startsWith(Utils.HTTP) || text.startsWith(Utils.HTTPS));
    }

    @Override
    public boolean handle(long chatId, String text) {
        if (!isValidLink(text)) {
            telegramBot.execute(new SendMessage(chatId, botMessageProperties.getInvalidLink()));
            return true;
        }

        try {
            scrapperClient.deleteLink(chatId, new RemoveLinkRequest(text));
            telegramBot.execute(new SendMessage(chatId, botMessageProperties.getUntrackSuccess()));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                telegramBot.execute(new SendMessage(chatId, botMessageProperties.getLinkNot()));
            } else {
                telegramBot.execute(new SendMessage(chatId, "Ошибка при удалении ссылки."));
            }
        } finally {
            userStateStorage.clearState(chatId);
        }
        return true;
    }
}
