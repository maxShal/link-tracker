/*
package backend.academy.linktracker.scrapper.service.cache;

import backend.academy.linktracker.scrapper.configuration.properties.ValkeyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValkeyTrackingService {

    private final ValkeyProperties valkeyProperties;

    private final StringRedisTemplate stringRedisTemplate;

    public void invalidate(long chatId) {
        stringRedisTemplate.convertAndSend(valkeyProperties.getChannel(), String.valueOf(chatId));
    }
}
*/
