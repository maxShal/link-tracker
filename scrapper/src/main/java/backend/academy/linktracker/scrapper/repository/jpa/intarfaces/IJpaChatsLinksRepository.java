package backend.academy.linktracker.scrapper.repository.jpa.intarfaces;

import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatsLinksEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkChatId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IJpaChatsLinksRepository extends JpaRepository<ChatsLinksEntity, LinkChatId> {
    List<ChatsLinksEntity> findByChatsEntityId(Long chatId);
    Optional<ChatsLinksEntity> findByChatsEntityIdAndLinksEntityUrl(Long chatsEntityId, String url);
    void deleteById(LinkChatId id);
}
