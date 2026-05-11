package backend.academy.linktracker.scrapper.service.cache;

import backend.academy.linktracker.scrapper.exception.errors.ChatNotFoundException;
import backend.academy.linktracker.scrapper.model.response.LinkResponse;
import backend.academy.linktracker.scrapper.model.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientSideCachingService {

    private final ValkeyTrackingService valkeyTrackingService;

    private final ILinksRepository linksRepository;

    private final ITgChatRepository chatRepository;

    private final Cache<Long, ListLinksResponse> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    public ListLinksResponse getAllLinks(long chatId, int page, int size) {
        ListLinksResponse allLinksList = cache.get(chatId, id -> {
            valkeyTrackingService.track(id);

            if (!chatRepository.existsChats(id)) {
                throw new ChatNotFoundException("Chat: " + id + "not found");
            }

            var links = linksRepository.findAllLinks(id).stream()
                    .map(link -> new LinkResponse(link.id(), link.url(), link.tags()))
                    .toList();

            return new ListLinksResponse(links, links.size());
        });

        return paginate(allLinksList, page, size);
    }

    public void evict(long chatId) {
        cache.invalidate(chatId);
    }

    private ListLinksResponse paginate(ListLinksResponse allLinksList, int page, int size) {
        var allLinks = allLinksList.links();

        int from = page * size;

        if (from >= allLinks.size()) {
            return new ListLinksResponse(List.of(), allLinks.size());
        }

        int to = Math.min(from + size, allLinks.size());

        return new ListLinksResponse(allLinks.subList(from, to), allLinks.size());
    }
}
