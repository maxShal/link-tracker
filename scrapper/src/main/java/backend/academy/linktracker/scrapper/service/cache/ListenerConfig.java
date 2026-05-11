package backend.academy.linktracker.scrapper.service.cache;

import backend.academy.linktracker.scrapper.configuration.properties.ValkeyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class ListenerConfig {

    private final ValkeyProperties valkeyProperties;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory, CacheInvalidationListener cacheInvalidationListener) {

        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(cacheInvalidationListener, new ChannelTopic(valkeyProperties.getChannel()));
        return container;
    }
}
