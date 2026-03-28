package backend.academy.linktracker.scrapper.repository.jpa.intarfaces;

import backend.academy.linktracker.scrapper.entity.LinksEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IJpaLinksRepository extends JpaRepository<LinksEntity, Long> {
    Optional<LinksEntity> findByUrl(String url);
}
