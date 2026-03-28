package backend.academy.linktracker.scrapper.repository.jpa.intarfaces;

import backend.academy.linktracker.scrapper.entity.LinkTagEntity;
import backend.academy.linktracker.scrapper.entity.LinkTagId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IJpaTagsLinksRepository extends JpaRepository<LinkTagEntity, LinkTagId> {
    Optional<LinkTagEntity> findByLinksEntityId(Long linksEntityId);

    boolean existsById(LinkTagId id);

    Optional<LinkTagEntity> findByLinksEntityIdAndTagsEntityTag(Long linksEntityId, String tag);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from LinkTagEntity lt where lt.linksEntity.id =:linkId")
    void deleteByLinkId(@Param("linkId") Long linkId);
}
