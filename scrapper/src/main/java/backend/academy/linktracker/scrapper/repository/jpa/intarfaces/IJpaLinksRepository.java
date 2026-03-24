package backend.academy.linktracker.scrapper.repository.jpa.intarfaces;

import backend.academy.linktracker.scrapper.repository.jpa.entity.LinksEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IJpaLinksRepository extends JpaRepository<LinksEntity,Long> {
    Optional<LinksEntity> findByUrl(String url);
}
