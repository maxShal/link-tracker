package backend.academy.linktracker.scrapper.service.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class ListenerConfig {
    private static final String CHANNEL = "links:invalidate";

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory, CacheInvalidationListener cacheInvalidationListener) {

        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(cacheInvalidationListener, new ChannelTopic(CHANNEL));
        return container;
    }
}
