package backend.academy.linktracker.ai.configuration.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka-consumer")
public record KafkaConsumerProperties(
    @NotBlank String topic,
    int partitions,
    short replicas,
    //@NotBlank String dlqTopic,
    long retryAttempts,
    long retryBackoffMs
) {
}
