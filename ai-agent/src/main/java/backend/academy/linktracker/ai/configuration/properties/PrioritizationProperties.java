package backend.academy.linktracker.ai.configuration.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai-agent.prioritization")
public record PrioritizationProperties(List<String> highKeywords, List<String> lowKeywords) {}
