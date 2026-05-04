package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.errors.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.errors.LinkAlreadyExistException;
import backend.academy.linktracker.scrapper.exception.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.model.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.model.response.LinkResponse;
import backend.academy.linktracker.scrapper.model.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LinksService {
    private final ILinksRepository linksRepository;

    private final ITgChatRepository chatRepository;

    @CacheEvict(value = "links", key = "#chatId")
    public LinkResponse addLink(Long chatId, AddLinkRequest addLinkRequest) {

        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат" + chatId + " не найден");
        }
        if (linksRepository.existsLink(chatId, addLinkRequest.link())) {
            throw new LinkAlreadyExistException("Ссылка" + addLinkRequest.link() + "уже существует");
        }

        Link link = new Link(null, addLinkRequest.link(), addLinkRequest.tags(), Instant.now());

        Link saved = linksRepository.saveLink(chatId, link);
        return new LinkResponse(saved.id(), saved.url(), saved.tags());
    }

    @Cacheable(value = "links", key = "#chatId + ':' + #page + ':' + #size")
    public ListLinksResponse getAllLinks(long chatId, int page, int size) {
        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат" + chatId + " не найден");
        }
        var links = linksRepository.findAllLinks(chatId, page, size).stream()
                .map(link -> new LinkResponse(link.id(), link.url(), link.tags()))
                .toList();
        return new ListLinksResponse(links, links.size());
    }

    @CacheEvict(value = "links", key = "#chatId")
    public LinkResponse deleteLink(Long chatId, RemoveLinkRequest removeLinkRequest) {
        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат" + chatId + " не найден");
        }
        Link removed = linksRepository.deleteLink(chatId, removeLinkRequest.link());
        if (removed == null) {
            throw new LinkNotFoundException("Ссылка" + removeLinkRequest.link() + "не найдена");
        }

        return new LinkResponse(removed.id(), removed.url(), removed.tags());
    }

    public List<LinkForUpdateCheck> findAllForUpdateCheck(int page, int size) {

        Map<String, LinkForUpdateCheck> aggregated = new ConcurrentHashMap<>();

        for (Map.Entry<Long, List<Link>> chatEntry :
                linksRepository.findAllLinksGroupedByChatId(page, size).entrySet()) {
            Long chatId = chatEntry.getKey();

            for (Link link : chatEntry.getValue()) {
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

    public void updateLastUpdated(Long linkId, Instant lastUpdatedAt) {

        linksRepository.updateLinkByLinkId(linkId, lastUpdatedAt.atOffset(ZoneOffset.UTC));
    }
}
