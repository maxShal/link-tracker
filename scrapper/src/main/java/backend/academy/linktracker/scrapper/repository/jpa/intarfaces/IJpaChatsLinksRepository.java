package backend.academy.linktracker.scrapper.repository.jpa.intarfaces;

import backend.academy.linktracker.scrapper.entity.ChatsLinksEntity;
import backend.academy.linktracker.scrapper.entity.LinkChatId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IJpaChatsLinksRepository extends JpaRepository<ChatsLinksEntity, LinkChatId> {
    List<ChatsLinksEntity> findByChatsEntityId(Long chatId);

    Optional<ChatsLinksEntity> findByChatsEntityIdAndLinksEntityUrl(Long chatsEntityId, String url);

    void deleteById(LinkChatId id);

    boolean existsByLinksEntityId(Long linkId);
}
