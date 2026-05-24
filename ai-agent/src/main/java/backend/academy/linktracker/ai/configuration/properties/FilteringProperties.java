package backend.academy.linktracker.ai.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "app.ai-agent.filtering")
public record FilteringProperties(
    List<String> stopWords,
    List<String> excludedAuthors,
    int minLength
) {}
