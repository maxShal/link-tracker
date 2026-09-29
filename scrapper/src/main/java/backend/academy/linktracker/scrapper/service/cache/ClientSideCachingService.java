/*
package backend.academy.linktracker.scrapper.service.cache;

import backend.academy.linktracker.scrapper.model.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.service.LinksService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientSideCachingService {

    private final ValkeyTrackingService valkeyTrackingService;

    private final LinksService linksService;

    private final Cache<Long, ListLinksResponse> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    public ListLinksResponse getAllLinks(long chatId, int page, int size) {

        var cached = cache.getIfPresent(chatId);
        if (cached != null) {
            log.atInfo().addKeyValue("Cash", cached).log("Send from cash");
            return paginate(cached, page, size);
        }

        log.atInfo().addKeyValue("Cash", chatId).log("Cash: " + chatId + " is empty");

        var allLinksList = linksService.getAllLinks(chatId);
        cache.put(chatId, allLinksList);
        log.atInfo().addKeyValue("Cash", chatId).log("Cash put in " + chatId);

        return paginate(allLinksList, page, size);
    }

    public void evict(long chatId) {
        log.atInfo().addKeyValue("Valkey", "Cash").log("evict " + chatId);
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
*/
