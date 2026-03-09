package backend.academy.linktracker.scrapper.repository.interfaces;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import java.time.Instant;
import java.util.List;

public interface ILinksRepository {
    Link saveLink(Long chatId, Link link);

    boolean existsLink(Long chatId, String url);

    Link deleteLink(Long chatId, String url);

    List<Link> findAllLinks(Long chatId);

    List<LinkForUpdateCheck> findAllForUpdateCheck();

    void updateLastUpdated(Long linkId, Instant lastUpdatedAt);
}
