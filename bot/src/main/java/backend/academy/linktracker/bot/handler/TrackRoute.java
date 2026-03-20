package backend.academy.linktracker.bot.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackRoute {
    private final List<TrackTagsHandler> trackTagsHandler;

    public void route(long chatId, String text) {
        for (TrackTagsHandler trackTagsHandler : trackTagsHandler) {
            if (trackTagsHandler.supports(chatId)) {
                trackTagsHandler.handle(chatId, text);
                return;
            }
        }
    }
}
