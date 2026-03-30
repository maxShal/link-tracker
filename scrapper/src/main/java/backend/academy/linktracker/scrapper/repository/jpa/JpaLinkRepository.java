package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatsEntity;
import backend.academy.linktracker.scrapper.entity.ChatsLinksEntity;
import backend.academy.linktracker.scrapper.entity.LinkChatId;
import backend.academy.linktracker.scrapper.entity.LinksEntity;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITagsRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaChatsLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaTagsLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaTgChatRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.repository-type", havingValue = "jpa")
@RequiredArgsConstructor
public class JpaLinkRepository implements ILinksRepository {

    private final IJpaLinksRepository jpaLinksRepository;
    private final IJpaTagsLinksRepository jpaTagsLinksRepository;
    private final IJpaChatsLinksRepository jpaChatsLinksRepository;
    private final IJpaTgChatRepository jpaTgChatsRepository;
    private final ITagsRepository jpaTagsRepository;

    @Transactional
    @Override
    public void updateLink(String url, OffsetDateTime updatedAt) {
        LinksEntity linksEntity =
                jpaLinksRepository.findByUrl(url).orElseThrow(() -> new RuntimeException("Links not found!"));

        linksEntity.setLastUpdatedAt(updatedAt);
    }

    @Override
    @Transactional
    public Link saveLink(Long chatId, Link link) {

        LinksEntity linkEntity = jpaLinksRepository.findByUrl(link.url()).orElseGet(() -> {
            LinksEntity newLink = new LinksEntity();
            newLink.setUrl(link.url());
            newLink.setLastUpdatedAt(OffsetDateTime.now());
            return jpaLinksRepository.saveAndFlush(newLink);
        });

        ChatsEntity chatsEntity =
                jpaTgChatsRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat Not Found"));

        LinkChatId id = new LinkChatId(chatId, linkEntity.getId());
        if (!jpaChatsLinksRepository.existsById(id)) {
            ChatsLinksEntity chatsLinksEntity = new ChatsLinksEntity(id, chatsEntity, linkEntity);
            jpaChatsLinksRepository.save(chatsLinksEntity);
        }

        if (link.tags() != null && !link.tags().isEmpty()) {
            jpaTagsRepository.saveTag(linkEntity.getId(), link.tags());
        }

        return mapToModel(linkEntity);
    }

    @Override
    @Transactional
    public boolean existsLink(Long chatId, String url) {
        return jpaChatsLinksRepository
                .findByChatsEntityIdAndLinksEntityUrl(chatId, url)
                .isPresent();
    }

    @Transactional
    @Override
    public Link deleteLink(Long chatId, String url) {

        return jpaChatsLinksRepository
                .findByChatsEntityIdAndLinksEntityUrl(chatId, url)
                .map(relation -> {
                    LinksEntity entity = relation.getLinksEntity();
                    Long linkId = entity.getId();
                    Link link = mapToModel(entity);
                    jpaChatsLinksRepository.delete(relation);
                    if (!jpaChatsLinksRepository.existsByLinksEntityId(linkId)) {
                        jpaTagsLinksRepository.deleteByLinkId(linkId);
                        jpaLinksRepository.delete(entity);
                    }
                    return link;
                })
                .orElse(null);
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
                        Collectors.mapping(rel -> mapToModel(rel.getLinksEntity()), Collectors.toList())));
    }

    private Link mapToModel(LinksEntity entity) {
        return new Link(
                entity.getId(),
                entity.getUrl(),
                jpaTagsRepository.findAllTagsByLinkId(entity.getId()),
                entity.getLastUpdatedAt() != null ? entity.getLastUpdatedAt().toInstant() : null);
    }
}
