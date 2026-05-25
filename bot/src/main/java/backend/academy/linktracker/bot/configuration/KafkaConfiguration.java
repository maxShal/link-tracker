package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.configuration.properties.ConsumerKafkaProperties;
import backend.academy.linktracker.bot.model.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ConsumerKafkaProperties.class)
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
public class KafkaConfiguration {
    private final KafkaProperties kafkaProperties;

    private final ConsumerKafkaProperties consumerKafkaProperties;

    /*    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        var recover = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(consumerKafkaProperties.dlqTopic(), record.partition()));

        var backOff = new FixedBackOff(
                consumerKafkaProperties.retryBackoffMs(), Math.max(0, consumerKafkaProperties.retryAttempts() - 1));

        var errorHandler = new DefaultErrorHandler(recover, backOff);

        errorHandler.addNotRetryableExceptions(
                DeserializationException.class,
                MessageConversionException.class,
                ConversionException.class,
                LinkUpdateException.class);

        errorHandler.setLogLevel(KafkaException.Level.WARN);
        return errorHandler;
    }*/

    @Bean("defaultFactory")
    public ConcurrentKafkaListenerContainerFactory<String, LinkUpdate> kafkaListenerContainerFactory(
            ConsumerFactory<String, LinkUpdate> consumerFactory /*, DefaultErrorHandler errorHandler*/) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, LinkUpdate>();
        factory.setConsumerFactory(consumerFactory);
        // factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public <M> ConsumerFactory<String, M> consumerFactory() {
        var props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, UpdateDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);

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
