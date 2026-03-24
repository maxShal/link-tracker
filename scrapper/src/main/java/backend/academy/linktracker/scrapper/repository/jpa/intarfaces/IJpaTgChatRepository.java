package backend.academy.linktracker.scrapper.repository.jpa.intarfaces;

import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IJpaTgChatRepository extends JpaRepository<ChatsEntity, Long> {
}
