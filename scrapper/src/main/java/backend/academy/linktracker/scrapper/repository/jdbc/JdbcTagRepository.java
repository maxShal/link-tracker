package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.interfaces.ITagsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.repository-type", havingValue = "jdbc")
@RequiredArgsConstructor
public class JdbcTagRepository implements ITagsRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<TagsRow> rowMapper = (rs, rowNum) -> new TagsRow(rs.getLong("id"), rs.getString("tag"));

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

    @Override
    public Tag deleteTag(Long chatId, String url, String tag) {

        Long linkId = jdbcTemplate.queryForObject("""
        SELECT lc.link_id
        FROM link_chat lc
        join links l on l.id = lc.link_id
        WHERE lc.chat_id = ? AND l.url = ?
        """, Long.class, chatId, url);

        List<TagsRow> tags = jdbcTemplate.query("""
            select t.id, t.tag
            from tags t
            join link_tag lt on lt.tag_id = t.id
            where lt.link_id = ? and t.tag = ?
        """, rowMapper, linkId, tag);

        if (tags.isEmpty()) return null;

        TagsRow row = tags.getFirst();

        Tag tagFromSql = new Tag(row.id(), row.tag());
        jdbcTemplate.update("""
                DELETE FROM link_tag
                WHERE link_id = ? AND tag_id = ?
        """, linkId, tagFromSql.id());

        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM link_tag
            WHERE tag_id = ?
            """, Integer.class, row.id());

        if (count != null && count == 0) {
            jdbcTemplate.update("DELETE FROM tags WHERE id = ?", row.id());
        }
        return tagFromSql;
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
               RETURNING id
        """, Long.class, tag);
    }

    private record TagsRow(Long id, String tag) {}
}
