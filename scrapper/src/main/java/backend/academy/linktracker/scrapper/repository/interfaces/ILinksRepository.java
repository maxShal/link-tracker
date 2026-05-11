package backend.academy.linktracker.scrapper.repository.interfaces;

import backend.academy.linktracker.scrapper.model.Link;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface ILinksRepository {

    void updateLinkByLinkId(Long linkId, OffsetDateTime updatedAt);

    Link saveLink(Long chatId, Link link);

    boolean existsLink(Long chatId, String url);

    Link deleteLink(Long chatId, String url);

    List<Link> findAllLinks(Long chatId, int page, int size);

    List<Link> findAllLinks(Long chatId);

    Long findLinkIdByUrl(String url);

    Map<Long, List<Link>> findAllLinksGroupedByChatId(int page, int size);
}
