package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.repository-type", havingValue = "jdbc")
@RequiredArgsConstructor
public class JdbcLinkRepository implements ILinksRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Link> rowMapper = (rs, rowNum) -> new Link(
            rs.getLong("id"),
            rs.getString("url"),
            Arrays.asList((String[]) rs.getArray("tags").getArray()),
            rs.getTimestamp("last_updated_at").toInstant());

    @Override
    public Link saveLink(Long chatId, Link link) {
        Long linkId = jdbcTemplate.queryForObject(
                """
            INSERT INTO links (url, tags,last_updated_at) VALUES (?, ?, ?)
            ON CONFLICT (url) DO UPDATE SET url = EXCLUDED.url
            RETURNING id
        """,
                Long.class,
                link.url(),
                link.tags().toArray(new String[0]),
                link.lastUpdatedAt() != null ? Timestamp.from(link.lastUpdatedAt()) : null);

        jdbcTemplate.update("""
        INSERT INTO link_chat (chat_id, link_id) VALUES (?, ?   )
        ON CONFLICT DO NOTHING
        """, chatId, linkId);
        return new Link(linkId, link.url(), link.tags(), link.lastUpdatedAt());
    }

    @Override
    public boolean existsLink(Long chatId, String url) {
        Integer count = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM link_chat lc
        JOIN links l ON lc.link_id = l.id
        WHERE lc.chat_id = ? AND l.url = ?
    """, Integer.class, chatId, url);
        return count != null && count > 0;
    }

    @Override
    public Link deleteLink(Long chatId, String url) {
        Link link = jdbcTemplate.queryForObject("""
            SELECT l.*
            FROM links l
            JOIN link_chat lc ON l.id = lc.link_id
            WHERE lc.chat_id = ? AND l.url = ?
        """, rowMapper, chatId, url);

        assert link != null;
        jdbcTemplate.update("""
            DELETE FROM link_chat
            WHERE chat_id = ? AND link_id = ?
        """, chatId, link.id());

        return link;
    }

    @Override
    public List<Link> findAllLinks(Long chatId) {
        return jdbcTemplate.query("""
                SELECT l.*
                FROM links l
                JOIN link_chat lc on lc.link_id = l.id
                where lc.chat_id = ?
                """, rowMapper, chatId);
    }

    @Override
    public Map<Long, List<Link>> findAllLinksGroupedByChatId() {
        return jdbcTemplate.query("""
            SELECT lc.chat_id, l.*
            FROM links l
            JOIN link_chat lc ON l.id = lc.link_id
        """, rs -> {
            Map<Long, List<Link>> map = new HashMap<>();

            while (rs.next()) {
                Long chatId = rs.getLong("chat_id");

                Link link = new Link(
                        rs.getLong("id"),
                        rs.getString("url"),
                        List.of(rs.getString("tags")),
                        rs.getTimestamp("last_updated_at").toInstant());

                map.computeIfAbsent(chatId, k -> new ArrayList<>()).add(link);
            }

            return map;
        });
    }
}
