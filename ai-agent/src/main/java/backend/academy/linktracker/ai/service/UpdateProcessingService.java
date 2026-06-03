package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.model.FilteredLinkUpdate;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateProcessingService {

    private final FilteringService filteringService;
    private final SummarizationService summarizationService;

    public Optional<FilteredLinkUpdate> process(RawLinkUpdate rawLinkUpdate) {

        if (!filteringService.isApproveFilter(rawLinkUpdate)) {
            return Optional.empty();
        }

        String description = summarizationService.summarization(rawLinkUpdate.description());

        String fullDescription = """
                           %n\
                           Автор: %s%n\
                           %s%n\
                           """.formatted(rawLinkUpdate.author(), description);

        return Optional.of(new FilteredLinkUpdate(
                rawLinkUpdate.id(), rawLinkUpdate.url(), fullDescription, rawLinkUpdate.tgChatIds(), "HIGH"));
    }
}
