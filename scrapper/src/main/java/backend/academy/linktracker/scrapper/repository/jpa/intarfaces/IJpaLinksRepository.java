package backend.academy.linktracker.scrapper.repository.jpa.intarfaces;

import backend.academy.linktracker.scrapper.entity.LinksEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IJpaLinksRepository extends JpaRepository<LinksEntity, Long> {
    Optional<LinksEntity> findByUrl(String url);

    @Query("select l.id from LinksEntity l where l.url = :url")
    Optional<Long> findIdByUrl(@Param("url") String url);
}
