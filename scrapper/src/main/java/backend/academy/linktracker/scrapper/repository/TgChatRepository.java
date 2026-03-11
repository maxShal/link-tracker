package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class TgChatRepository implements ITgChatRepository {

    private final Set<Long> chats = ConcurrentHashMap.newKeySet();

    @Override
    public void saveChat(Long id) {
        chats.add(id);
    }

    @Override
    public boolean existsChats(Long id) {
        return chats.contains(id);
    }

    @Override
    public void deleteChat(Long id) {
        chats.remove(id);
    }

    public void clear() {
        chats.clear();
    }
}
