package backend.academy.linktracker.ai.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.validation.annotation.Validated;

@Configuration
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.kafka-producer")
public class KafkaProducerProperties {
    String topic;
    // String dlqTopic;
    int partitions;
    short replicas;

    public KafkaAdmin.NewTopics toNewTopic() {
        return new KafkaAdmin.NewTopics(
                new NewTopic(topic, partitions, replicas) /*, new NewTopic(dlqTopic, partitions, replicas)*/);
    }
}
