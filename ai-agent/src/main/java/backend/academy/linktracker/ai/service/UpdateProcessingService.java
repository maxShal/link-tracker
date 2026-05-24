package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.model.FilteredLinkUpdate;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateProcessingService {

    private final FilteringService filteringService;
    private final SummarizationService summarizationService;

    public Optional<FilteredLinkUpdate> process(RawLinkUpdate rawLinkUpdate) {

        if(!filteringService.isApproveFilter(rawLinkUpdate)){
            return Optional.empty();
        }

        String description = summarizationService.summarization(rawLinkUpdate.description());

        return Optional.of(new FilteredLinkUpdate(
            rawLinkUpdate.id(),
            rawLinkUpdate.url(),
            description,
            rawLinkUpdate.tgChatIds(),
            "HIGH"
        ));
    }
}
