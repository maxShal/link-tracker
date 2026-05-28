package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.properties.PrioritizationProperties;
import backend.academy.linktracker.ai.model.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrioritizationService {
    private final PrioritizationProperties prioritizationProperties;

    public Priority getPriority(String text)
    {
        String lowText =  text.toLowerCase();

        boolean hasHigh = prioritizationProperties.highKeywords().stream()
            .map(String::toLowerCase)
            .anyMatch(lowText::contains);

        if(hasHigh) return Priority.HIGH;

        boolean hasLow = prioritizationProperties.lowKeywords().stream()
            .map(String::toLowerCase)
            .anyMatch(lowText::contains);
        if(hasLow) return Priority.LOW;

        return Priority.MEDIUM;

    }
}
