package backend.academy.linktracker.scrapper.repository.jpa.intarfaces;

import backend.academy.linktracker.scrapper.repository.jpa.entity.TagsEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IJpaTagsRepository extends JpaRepository<TagsEntity, Long> {

    Optional<TagsEntity> findByTag(String tag);

    // Optional<TagsEntity> findAllTagsByLinksId(Long linksId);

    // Optional<TagsEntity> findAllTagsById(Long id);

    // boolean existsByTag(String tag);
}
