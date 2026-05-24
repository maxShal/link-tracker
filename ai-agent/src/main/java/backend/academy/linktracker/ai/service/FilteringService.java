package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.properties.FilteringProperties;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilteringService {
    private final FilteringProperties filteringProperties;

    public boolean isApproveFilter(RawLinkUpdate rawLinkUpdate) {
        if(rawLinkUpdate.description()==null || rawLinkUpdate.description().isEmpty())
            return false;
        if(rawLinkUpdate.author()==null || rawLinkUpdate.author().isEmpty())
            return false;

        String description=rawLinkUpdate.description();

        boolean hasStopWord = filteringProperties.stopWords().stream()
            .map(String::toLowerCase)
            .anyMatch(description::contains);

        if(hasStopWord)
            return false;

        boolean excludedAuthors = filteringProperties.excludedAuthors().stream()
            .anyMatch(author -> author.equalsIgnoreCase(rawLinkUpdate.author()));

        if(excludedAuthors)
            return false;

        return rawLinkUpdate.description().length()>=filteringProperties.minLength();
    }
}
