package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import backend.academy.linktracker.scrapper.service.strateges.IStrategyHandler;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MetadataService {

    private final List<IStrategyHandler> metadataHandlers;

    public Optional<LinkUpdateResponse> getLastUpdated(String url) {
        return metadataHandlers.stream()
                .filter(handler -> handler.patternCheck(url))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неподдерживаемая ссылка: " + url))
                .getLastUpdated(url);
    }
}
