package backend.academy.linktracker.scrapper.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValkeyTrackingService {

    private static final String CHANNEL = "links:invalidate";

    private final StringRedisTemplate stringRedisTemplate;

    public void track(long chatId) {}

    public void invalidate(long chatId) {
        stringRedisTemplate.convertAndSend(CHANNEL, String.valueOf(chatId));
    }
}
