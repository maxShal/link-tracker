package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.errors.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.errors.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TgChatService {

    private final ITgChatRepository tgChatRepository;

    public void addChat(Long id) {
        if (tgChatRepository.existsChats(id)) {
            throw new ChatAlreadyExistsException("Чат" + id + " уже зарегистрирован");
        }
        tgChatRepository.saveChat(id);
    }

    public void removeChat(Long id) {
        if (!tgChatRepository.existsChats(id)) {
            throw new ChatNotFoundException("Чат" + id + " не найден");
        }
        tgChatRepository.deleteChat(id);
    }

    public boolean existsChat(Long id) {
        return tgChatRepository.existsChats(id);
    }
}
