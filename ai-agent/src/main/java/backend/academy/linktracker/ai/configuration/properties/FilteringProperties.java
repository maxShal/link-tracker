package backend.academy.linktracker.ai.configuration.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai-agent.filtering")
public record FilteringProperties(List<String> stopWords, List<String> excludedAuthors, int minLength) {}
