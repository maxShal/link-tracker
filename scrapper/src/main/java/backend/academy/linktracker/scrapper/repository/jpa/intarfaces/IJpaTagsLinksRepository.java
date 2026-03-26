package backend.academy.linktracker.scrapper.repository.jpa.intarfaces;

import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkTagEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkTagId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IJpaTagsLinksRepository extends JpaRepository<LinkTagEntity, LinkTagId> {
    List<LinkTagEntity> findByLinksEntityId(Long linksEntityId);

    boolean existsById(LinkTagId id);

    Optional<LinkTagEntity> findByLinksEntityIdAndTagsEntityTag(Long linksEntityId, String tag);
    // void deleteByLinkId(Long linkId);
}
