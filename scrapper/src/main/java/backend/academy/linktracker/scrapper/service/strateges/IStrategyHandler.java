package backend.academy.linktracker.scrapper.service.strateges;

import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import java.util.Optional;

public interface IStrategyHandler {
    Optional<LinkUpdateResponse> getLastUpdated(String url);

    boolean patternCheck(String url);
}
