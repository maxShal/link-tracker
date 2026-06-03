package backend.academy.linktracker.ai.configuration;

import backend.academy.linktracker.ai.configuration.properties.KafkaConsumerProperties;
import backend.academy.linktracker.ai.configuration.properties.KafkaProducerProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@RequiredArgsConstructor
@Configuration
public class KafkaTopicConfiguration {
    private final KafkaProperties kafkaProperties;
    private final KafkaConsumerProperties consumerProperties;
    private final KafkaProducerProperties producerProperties;

    @Bean
    KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers()));
    }

    @Bean
    NewTopic rawUpdatesTopic() {
        return TopicBuilder.name(consumerProperties.topic())
                .partitions(producerProperties.getPartitions())
                .replicas(producerProperties.getReplicas())
                .build();
    }

    @Bean
    NewTopic processedUpdatesTopic() {
        return TopicBuilder.name(producerProperties.getTopic())
                .partitions(producerProperties.getPartitions())
                .replicas(producerProperties.getReplicas())
                .build();
    }
}
