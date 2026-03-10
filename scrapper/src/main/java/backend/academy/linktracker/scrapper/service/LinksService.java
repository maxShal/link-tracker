package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.errors.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.errors.LinkAlreadyExistException;
import backend.academy.linktracker.scrapper.exception.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import backend.academy.linktracker.scrapper.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.response.LinkResponse;
import backend.academy.linktracker.scrapper.response.ListLinksResponse;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LinksService {
    private final ILinksRepository linksRepository;

    private final ITgChatRepository chatRepository;

    private final AtomicLong idGenerator = new AtomicLong(0);

    public LinkResponse addLink(Long chatId, AddLinkRequest addLinkRequest) {

        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат" + chatId + " не найден");
        }
        if (linksRepository.existsLink(chatId, addLinkRequest.link())) {
            throw new LinkAlreadyExistException("Ссылка" + addLinkRequest.link() + "уже существует");
        }

        Link link = new Link(
                idGenerator.incrementAndGet(), addLinkRequest.link(), addLinkRequest.tags(), addLinkRequest.filters(), null);

        Link saved = linksRepository.saveLink(chatId, link);
        return new LinkResponse(saved.id(), saved.url(), saved.tags(), saved.filters());
    }

    public ListLinksResponse getAllLinks(long chatId) {
        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат" + chatId + " не найден");
        }
        var links = linksRepository.findAllLinks(chatId).stream()
                .map(link -> new LinkResponse(link.id(), link.url(), link.tags(), link.filters()))
                .toList();
        return new ListLinksResponse(links, links.size());
    }

    public LinkResponse deleteLink(Long chatId, RemoveLinkRequest removeLinkRequest) {
        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат" + chatId + " не найден");
        }
        Link removed = linksRepository.deleteLink(chatId, removeLinkRequest.link());
        if (removed == null) {
            throw new LinkNotFoundException("Ссылка" + removeLinkRequest.link() + "не найдена");
        }

        return new LinkResponse(removed.id(), removed.url(), removed.tags(), removed.filters());
    }
}
