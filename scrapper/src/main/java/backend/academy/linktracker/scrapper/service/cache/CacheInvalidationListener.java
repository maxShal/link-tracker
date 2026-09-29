/*
package backend.academy.linktracker.scrapper.service.cache;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheInvalidationListener implements MessageListener {

    private final ClientSideCachingService clientSideCachingService;

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        String body = new String(message.getBody());
        long chatId = Long.parseLong(body);

        clientSideCachingService.evict(chatId);
    }
}
*/
