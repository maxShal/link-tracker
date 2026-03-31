package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.exception.errors.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.errors.LinkAlreadyExistException;
import backend.academy.linktracker.scrapper.exception.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.model.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.model.response.LinkResponse;
import backend.academy.linktracker.scrapper.model.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinksServiceTest {

    private static final String LINK = "https://github.com/owner/repo";

    @Mock
    private SchedulerProperties properties;

    @Mock
    private ILinksRepository linksRepository;

    @Mock
    private ITgChatRepository chatRepository;

    @InjectMocks
    private LinksService linksService;

    @Test
    void addLinkShouldThrowWhenChatNotFound() {
        when(chatRepository.existsChats(1L)).thenReturn(false);

        assertThrows(ChatNotFoundException.class, () -> linksService.addLink(1L, new AddLinkRequest(LINK, List.of())));
    }

    @Test
    void addLinkShouldThrowWhenLinkAlreadyExists() {
        when(chatRepository.existsChats(1L)).thenReturn(true);
        when(linksRepository.existsLink(1L, LINK)).thenReturn(true);

        assertThrows(
                LinkAlreadyExistException.class, () -> linksService.addLink(1L, new AddLinkRequest(LINK, List.of())));
    }

    @Test
    void addLinkShouldSaveAndReturnResponse() {
        when(chatRepository.existsChats(1L)).thenReturn(true);
        when(linksRepository.existsLink(1L, LINK)).thenReturn(false);

        Link saved = new Link(1L, LINK, List.of("tag"), null);
        when(linksRepository.saveLink(eq(1L), any(Link.class))).thenReturn(saved);

        LinkResponse response = linksService.addLink(1L, new AddLinkRequest(LINK, List.of("tag")));

        assertEquals(LINK, response.url());
    }

    @Test
    void getAllLinksShouldThrowWhenChatNotFound() {
        when(chatRepository.existsChats(1L)).thenReturn(false);

        assertThrows(
                ChatNotFoundException.class,
                () -> linksService.getAllLinks(1L, properties.getPage(), properties.getSize()));
    }

    @Test
    void getAllLinksShouldReturnListResponse() {
        when(chatRepository.existsChats(1L)).thenReturn(true);
        when(linksRepository.findAllLinks(1L, properties.getPage(), properties.getSize()))
                .thenReturn(List.of(new Link(1L, LINK, List.of(), Instant.now())));

        ListLinksResponse response = linksService.getAllLinks(1L, properties.getPage(), properties.getSize());

        assertEquals(1, response.size());
        assertEquals(LINK, response.links().get(0).url());
    }

    @Test
    void deleteLinkShouldThrowWhenChatNotFound() {
        when(chatRepository.existsChats(1L)).thenReturn(false);

        assertThrows(ChatNotFoundException.class, () -> linksService.deleteLink(1L, new RemoveLinkRequest(LINK)));
    }

    @Test
    void deleteLinkShouldThrowWhenLinkNotFound() {
        when(chatRepository.existsChats(1L)).thenReturn(true);
        when(linksRepository.deleteLink(1L, LINK)).thenReturn(null);

        assertThrows(LinkNotFoundException.class, () -> linksService.deleteLink(1L, new RemoveLinkRequest(LINK)));
    }

    @Test
    void deleteLinkShouldReturnRemovedLink() {
        when(chatRepository.existsChats(1L)).thenReturn(true);
        when(linksRepository.deleteLink(1L, LINK)).thenReturn(new Link(1L, LINK, List.of(), Instant.now()));

        LinkResponse response = linksService.deleteLink(1L, new RemoveLinkRequest(LINK));

        assertEquals(LINK, response.url());
    }
}
