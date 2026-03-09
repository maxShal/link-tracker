package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class LinksRepository implements ILinksRepository {

    private final Map<Long, Map<String, Link>> links = new ConcurrentHashMap<>();

    @Override
    public Link saveLink(Long chatId, Link link) {
        links.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>()).put(link.url(), link);
        return link;
    }

    @Override
    public boolean existsLink(Long chatId, String url) {
        return links.getOrDefault(chatId, Map.of()).containsKey(url);
    }

    @Override
    public Link deleteLink(Long chatId, String url) {
        Map<String, Link> map = links.get(chatId);
        if (map == null) {
            return null;
        }
        return map.remove(url);
    }

    @Override
    public List<Link> findAllLinks(Long chatId) {
        return links.getOrDefault(chatId, Map.of()).values().stream().toList();
    }

    @Override
    public List<LinkForUpdateCheck> findAllForUpdateCheck() {
        Map<String, LinkForUpdateCheck> aggregated = new ConcurrentHashMap<>();

        for (Map.Entry<Long, Map<String, Link>> chatEntry : links.entrySet()) {
            Long chatId = chatEntry.getKey();

            for (Link link : chatEntry.getValue().values()) {
                aggregated.compute(link.url(), (url, existing) -> {
                    if (existing == null) {
                        List<Long> chatIds = new ArrayList<>();
                        chatIds.add(chatId);

                        return new LinkForUpdateCheck(link.id(), link.url(), chatIds, link.lastUpdatedAt());
                    } else {
                        List<Long> updatedChatIds = new ArrayList<>(existing.tgChatIds());
                        if (!updatedChatIds.contains(chatId)) {
                            updatedChatIds.add(chatId);
                        }

                        return new LinkForUpdateCheck(
                                existing.id(), existing.url(), updatedChatIds, existing.lastUpdatedAt());
                    }
                });
            }
        }

        return aggregated.values().stream().toList();
    }

    @Override
    public void updateLastUpdated(Long linkId, Instant lastUpdatedAt) {
        for (Map<String, Link> chatLinks : links.values()) {
            for (Map.Entry<String, Link> entry : chatLinks.entrySet()) {
                Link oldLink = entry.getValue();

                if (oldLink.id().equals(linkId)) {
                    Link updatedLink =
                            new Link(oldLink.id(), oldLink.url(), oldLink.tags(), oldLink.filters(), lastUpdatedAt);
                    entry.setValue(updatedLink);
                }
            }
        }
    }
}
