package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.properties.SummarizationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SummarizationService {
    private final SummarizationProperties summarizationProperties;

    public String summarization(String text)
    {
        if (text.isEmpty())
        {
            return null;
        }

        if(text.length()<=summarizationProperties.threshold())
        {
            return text;
        }
        return text.substring(0, summarizationProperties.threshold())+"...";


    }
}
