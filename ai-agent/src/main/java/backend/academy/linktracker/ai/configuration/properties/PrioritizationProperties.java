package backend.academy.linktracker.ai.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "app.ai-agent.prioritization")
public record PrioritizationProperties (
    List<String> highKeywords,
    List<String> lowKeywords)
{}
