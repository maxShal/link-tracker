package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.exception.errors.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.errors.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TgChatServiceTest {

    @Mock
    private ITgChatRepository tgChatRepository;

    @InjectMocks
    private TgChatService tgChatService;

    @Test
    void addChatShouldSaveWhenChatDoesNotExist() {
        long chatId = 1L;
        when(tgChatRepository.existsChats(chatId)).thenReturn(false);

        tgChatService.addChat(chatId);

        verify(tgChatRepository).saveChat(chatId);
    }

    @Test
    void addChatShouldThrowWhenChatAlreadyExists() {
        long chatId = 1L;
        when(tgChatRepository.existsChats(chatId)).thenReturn(true);

        assertThrows(ChatAlreadyExistsException.class, () -> tgChatService.addChat(chatId));

        verify(tgChatRepository, never()).saveChat(anyLong());
    }

    @Test
    void removeChatShouldDeleteWhenChatExists() {
        long chatId = 2L;
        when(tgChatRepository.existsChats(chatId)).thenReturn(true);

        tgChatService.removeChat(chatId);

        verify(tgChatRepository).deleteChat(chatId);
    }

    @Test
    void removeChatShouldThrowWhenChatDoesNotExist() {
        long chatId = 2L;
        when(tgChatRepository.existsChats(chatId)).thenReturn(false);

        assertThrows(ChatNotFoundException.class, () -> tgChatService.removeChat(chatId));

        verify(tgChatRepository, never()).deleteChat(anyLong());
    }
}
