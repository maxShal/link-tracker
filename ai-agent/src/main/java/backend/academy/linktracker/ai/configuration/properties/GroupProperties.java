package backend.academy.linktracker.ai.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai-agent.grouping")
public record GroupProperties(
    int windowMs
) {}
