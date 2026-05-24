package backend.academy.linktracker.bot.configuration.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka")
public record ConsumerKafkaProperties(
        @NotBlank String topic,
        //@NotBlank String dlqTopic,
        @Min(1) long retryAttempts,
        @Min(0) long retryBackoffMs) {}
