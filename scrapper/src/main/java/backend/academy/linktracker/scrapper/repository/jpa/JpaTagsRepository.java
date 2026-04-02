package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatsLinksEntity;
import backend.academy.linktracker.scrapper.entity.LinkTagEntity;
import backend.academy.linktracker.scrapper.entity.LinkTagId;
import backend.academy.linktracker.scrapper.entity.LinksEntity;
import backend.academy.linktracker.scrapper.entity.TagsEntity;
import backend.academy.linktracker.scrapper.exception.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.interfaces.ITagsRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaChatsLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaTagsLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaTagsRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.repository-type", havingValue = "jpa")
@RequiredArgsConstructor
public class JpaTagsRepository implements ITagsRepository {

    private final IJpaTagsRepository jpaTagsRepository;
    private final IJpaTagsLinksRepository jpaTagsLinksRepository;
    private final IJpaLinksRepository jpaLinksRepository;
    private final IJpaChatsLinksRepository jpaChatsLinksRepository;

    @Transactional
    @Override
    public void saveTag(Long linkId, List<String> tags) {
        LinksEntity linksEntity =
                jpaLinksRepository.findById(linkId).orElseThrow(() -> new LinkNotFoundException("Link not found"));

        for (String tagName : tags) {
            TagsEntity tag = jpaTagsRepository
                    .findByTag(tagName)
                    .orElseGet(() -> jpaTagsRepository.save(new TagsEntity(null, tagName)));
            LinkTagId id = new LinkTagId(linkId, tag.getId());
            if (!jpaTagsLinksRepository.existsById(id)) {
                jpaTagsLinksRepository.save(new LinkTagEntity(id, linksEntity, tag));
            }
        }
    }

    @Override
    public boolean existsTag(Long linkId, String tag) {
        return jpaTagsLinksRepository
                .findByLinksEntityIdAndTagsEntityTag(linkId, tag)
                .isPresent();
    }

    @Override
    public List<String> findAllTagsByLinkId(Long linkId) {
        return jpaTagsLinksRepository.findByLinksEntityId(linkId).stream()
                .map(rel -> rel.getTagsEntity().getTag())
                .toList();
    }

    @Override
    @Transactional
    public Tag deleteTag(Long chatId, String url, String tagName) {

        ChatsLinksEntity chatLink = jpaChatsLinksRepository
                .findByChatsEntityIdAndLinksEntityUrl(chatId, url)
                .orElseThrow(() -> new LinkNotFoundException("Ссылка " + url + " не найдена у чата " + chatId));

        LinksEntity link = chatLink.getLinksEntity();

        TagsEntity tag = jpaTagsRepository
                .findByTag(tagName)
                .orElseThrow(() -> new IllegalArgumentException("Тег " + tagName + " не найден"));

        LinkTagId id = new LinkTagId(link.getId(), tag.getId());

        if (!jpaTagsLinksRepository.existsById(id)) {
            return null;
        }

        jpaTagsLinksRepository.deleteById(id);

        if (!jpaTagsLinksRepository.existsByTagsEntityId(tag.getId())) {
            jpaTagsRepository.delete(tag);
        }

        return new Tag(tag.getId(), tag.getTag());
    }
}
