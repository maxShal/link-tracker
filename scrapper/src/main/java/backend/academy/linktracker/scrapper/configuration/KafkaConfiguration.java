package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;


@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaProperties kafkaProperties;

    public static final  String GENERATE_KAFKA_TEMPLATE = "generateKafkaTemplate";

    @Bean(GENERATE_KAFKA_TEMPLATE)
    public KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate() {
        var properties = kafkaProperties.buildProducerProperties();

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        var factory = new DefaultKafkaProducerFactory<String,LinkUpdateRequest>(properties);
        return new KafkaTemplate<>(factory);
    }
}
