package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.repository.interfaces.ITagsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.repository-type", havingValue = "jdbc")
@RequiredArgsConstructor
public class JdbcTagRepository implements ITagsRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveTag(Long linkId, List<String> tags) {
        for (String tag : tags) {

            Long tagId = findTagId(tag);

            if (tagId == null) {
                tagId = createTag(tag);
            }

            if (!existsTag(linkId, tag)) {

                jdbcTemplate.update("""
                        INSERT INTO link_tag(link_id, tag_id)
                        VALUES (?, ?)
                        """, linkId, tagId);
            }
        }
    }

    @Override
    public boolean existsTag(Long linkId, String tag) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM link_tag lt
                JOIN tags t ON lt.tag_id = t.id
                WHERE lt.link_id = ?
                AND t.tag = ?
                """, Integer.class, linkId, tag);

        return count != null && count > 0;
    }

    @Override
    public List<String> findAllTagsByLinkId(Long linkId) {
        return jdbcTemplate.query("""
                SELECT t.tag
                FROM tags t
                JOIN link_tag lt ON lt.tag_id = t.id
                WHERE lt.link_id = ?
                """, (rs, rowNum) -> rs.getString("tag"), linkId);
    }

    private Long findTagId(String tag) {

        List<Long> ids = jdbcTemplate.query("""
                SELECT id
                FROM tags
                WHERE tag = ?
                """, (rs, rowNum) -> rs.getLong("id"), tag);

        return ids.isEmpty() ? null : ids.get(0);
    }

    private Long createTag(String tag) {
        return jdbcTemplate.queryForObject("""
               INSERT INTO tags(tag)
               VALUES (?)
               RETURNED id
        """, Long.class, tag);
    }
}
