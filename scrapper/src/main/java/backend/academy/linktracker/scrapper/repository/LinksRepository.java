package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
    public Map<Long, List<Link>> findAllLinksGroupedByChatId() {
        return links.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, e -> new ArrayList<>(e.getValue().values())));
    }

    public void clear() {
        links.clear();
    }
}
