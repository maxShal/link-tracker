package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatsEntity;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaTgChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.repository-type", havingValue = "jpa")
public class JpaTgChatsRepository implements ITgChatRepository {

    private final IJpaTgChatRepository tgChatRepository;

    @Override
    public void saveChat(Long id) {
        if (!tgChatRepository.existsById(id)) {
            tgChatRepository.save(new ChatsEntity(id));
        }
    }

    @Override
    public boolean existsChats(Long id) {
        return tgChatRepository.existsById(id);
    }

    @Override
    public void deleteChat(Long id) {
        tgChatRepository.deleteById(id);
    }
}
