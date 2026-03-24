package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatsEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatsLinksEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkChatId;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinksEntity;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaChatsLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaTgChatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "app.repository-type", havingValue = "jpa")
@RequiredArgsConstructor
public class JpaLinkRepository implements ILinksRepository {

    private final IJpaLinksRepository jpaLinksRepository;
    private final IJpaChatsLinksRepository jpaChatsLinksRepository;
    private final IJpaTgChatRepository  jpaTgChatsRepository;

    @Override
    @Transactional
    public Link saveLink(Long chatId, Link link) {

        LinksEntity linkEntity = new LinksEntity();
        linkEntity.setUrl(link.url());
        linkEntity.setTags(link.tags());
        linkEntity.setLastUpdatedAt(OffsetDateTime.now());

        jpaLinksRepository.save(linkEntity);

        ChatsEntity chatsEntity = jpaTgChatsRepository.findById(chatId)
            .orElseThrow(()->new RuntimeException("Chat Not Found"));

        LinkChatId id = new  LinkChatId(chatId, linkEntity.getId());
        if(!jpaChatsLinksRepository.existsById(id))
        {
            ChatsLinksEntity chatsLinksEntity = new ChatsLinksEntity(id, chatsEntity, linkEntity);
            jpaChatsLinksRepository.save(chatsLinksEntity);
        }

        return mapToModel(linkEntity);
    }

    @Override
    @Transactional
    public boolean existsLink(Long chatId, String url) {
        return jpaChatsLinksRepository.findByChatsEntityIdAndLinksEntityUrl(chatId, url).isPresent();
    }

    @Transactional
    @Override
    public Link deleteLink(Long chatId, String url) {
        ChatsLinksEntity relation = jpaChatsLinksRepository.findByChatsEntityIdAndLinksEntityUrl(chatId, url)
            .orElse(null);
        if (relation == null) {
            return null;
        }

        Link link = mapToModel(relation.getLinksEntity());
        jpaChatsLinksRepository.delete(relation);
        jpaLinksRepository.deleteById(link.id());
        return link;
    }
    @Transactional
    @Override
    public List<Link> findAllLinks(Long chatId) {
        return jpaChatsLinksRepository.findByChatsEntityId(chatId).stream()
            .map(relation -> mapToModel(relation.getLinksEntity()))
            .toList();
    }
    @Transactional
    @Override
    public Map<Long, List<Link>> findAllLinksGroupedByChatId() {
        return jpaChatsLinksRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                rel -> rel.getChatsEntity().getId(),
                Collectors.mapping(rel -> mapToModel(rel.getLinksEntity()), Collectors.toList())
            ));
    }

    private Link mapToModel(LinksEntity entity) {
        return new Link(
            entity.getId(),
            entity.getUrl(),
            entity.getTags(),
            entity.getLastUpdatedAt() != null ? entity.getLastUpdatedAt().toInstant() : null
        );
    }
}
