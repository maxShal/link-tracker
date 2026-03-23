package backend.academy.linktracker.scrapper.repository.interfaces;

import backend.academy.linktracker.scrapper.model.Link;
import java.util.List;
import java.util.Map;

public interface ILinksRepository {
    Link saveLink(Long chatId, Link link);

    boolean existsLink(Long chatId, String url);

    Link deleteLink(Long chatId, String url);

    List<Link> findAllLinks(Long chatId);

    Map<Long, List<Link>> findAllLinksGroupedByChatId();

    // List<LinkForUpdateCheck> findAllForUpdateCheck();

    // void updateLastUpdated(Long linkId, Instant lastUpdatedAt);
}
