package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.model.dto.LinkUpdate;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {
    private final KafkaProperties kafkaProperties;

    @Bean("defaultFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, LinkUpdate>> defaultFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, LinkUpdate>();
        factory.setConsumerFactory(consumerFactory(
                UpdateDeserializer.class, props -> props.put(ConsumerConfig.GROUP_ID_CONFIG, "default-consumer")));
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setAutoStartup(true);
        factory.setConcurrency(1);
        return factory;
    }

    private <M> ConsumerFactory<String, M> consumerFactory(
            Class<? extends Deserializer<M>> deserializerClass, Consumer<Map<String, Object>> propsModifier) {
        var props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass);

        propsModifier.accept(props);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    public static class UpdateDeserializer extends JacksonJsonDeserializer<LinkUpdate> {
        public UpdateDeserializer() {
            super(LinkUpdate.class);
            this.ignoreTypeHeaders();
            this.trustedPackages("*");
        }
    }
}
