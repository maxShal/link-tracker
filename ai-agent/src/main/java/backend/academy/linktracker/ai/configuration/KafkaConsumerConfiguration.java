package backend.academy.linktracker.ai.configuration;

import backend.academy.linktracker.ai.configuration.properties.KafkaConsumerProperties;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfiguration {

    private final KafkaProperties kafkaProperties;

    //private final KafkaConsumerProperties kafkaConsumerProperties;

    @Bean("defaultFactory")
    public ConcurrentKafkaListenerContainerFactory<String, RawLinkUpdate> kafkaListenerContainerFactory(
        ConsumerFactory<String, RawLinkUpdate> consumerFactory/*, DefaultErrorHandler errorHandler*/) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RawLinkUpdate>();
        factory.setConsumerFactory(consumerFactory);
        //factory.setCommonErrorHandler(errorHandler);
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

    public static class UpdateDeserializer extends JacksonJsonDeserializer<RawLinkUpdate> {
        public UpdateDeserializer() {
            super(RawLinkUpdate.class);
            this.ignoreTypeHeaders();
            this.trustedPackages("*");
        }
    }
}
