package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITagsRepository;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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

    private final ITagsRepository tagsRepository;

    private final RowMapper<LinksRow> rowMapper = (rs, rowNum) -> new LinksRow(
            rs.getLong("id"),
            rs.getString("url"),
            rs.getTimestamp("last_updated_at").toInstant().atOffset(ZoneOffset.UTC));

    @Override
    public void updateLink(String url, OffsetDateTime updatedAt) {
        jdbcTemplate.update("""
            UPDATE links SET  last_updated_at = ?
            WHERE url = ?
        """, updatedAt, url);
    }

    @Override
    public Link saveLink(Long chatId, Link link) {
        Long linkId = jdbcTemplate.queryForObject(
                """
            INSERT INTO links (url,last_updated_at) VALUES (?, ?)
            ON CONFLICT (url) DO UPDATE SET url = EXCLUDED.url
            RETURNING id
        """,
                Long.class,
                link.url(),
                link.lastUpdatedAt() != null ? Timestamp.from(link.lastUpdatedAt()) : null);

        jdbcTemplate.update("""
        INSERT INTO link_chat (chat_id, link_id) VALUES (?, ?   )
        ON CONFLICT DO NOTHING
        """, chatId, linkId);

        if (link.tags() != null && !link.tags().isEmpty()) {
            tagsRepository.saveTag(linkId, link.tags());
        }

        return new Link(linkId, link.url(), tagsRepository.findAllTagsByLinkId(linkId), link.lastUpdatedAt());
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
        List<LinksRow> links = jdbcTemplate.query("""
            SELECT l.id, l.url, l.last_updated_at
            FROM links l
            JOIN link_chat lc ON l.id = lc.link_id
            WHERE lc.chat_id = ? AND l.url = ?
        """, rowMapper, chatId, url);

        if (links.isEmpty()) return null;

        LinksRow row = links.getFirst();

        Link link = new Link(
                row.id(),
                row.url(),
                tagsRepository.findAllTagsByLinkId(row.id()),
                row.lastUpdatedAt() != null ? row.lastUpdatedAt().toInstant() : null);

        jdbcTemplate.update("""
            DELETE FROM link_chat
            WHERE chat_id = ? AND link_id = ?
        """, chatId, link.id());

        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM link_chat
            WHERE link_id = ?
            """, Integer.class, row.id());

        if (count != null && count == 0) {
            jdbcTemplate.update("DELETE FROM links WHERE id = ?", row.id());
        }

        return link;
    }

    @Override
    public List<Link> findAllLinks(Long chatId) {
        List<LinksRow> linksRows = jdbcTemplate.query("""
                SELECT l.id, l.url, l.last_updated_at
                FROM links l
                JOIN link_chat lc on lc.link_id = l.id
                where lc.chat_id = ?
                """, rowMapper, chatId);

        return linksRows.stream()
                .map(row -> new Link(
                        row.id(),
                        row.url(),
                        tagsRepository.findAllTagsByLinkId(row.id()),
                        row.lastUpdatedAt() != null ? row.lastUpdatedAt().toInstant() : null))
                .toList();
    }

    @Override
    public Map<Long, List<Link>> findAllLinksGroupedByChatId() {
        return jdbcTemplate.query("""
            SELECT lc.chat_id, l.id, l.url, l.last_updated_at
            FROM links l
            JOIN link_chat lc ON l.id = lc.link_id
        """, rs -> {
            Map<Long, List<Link>> map = new HashMap<>();

            while (rs.next()) {
                Long chatId = rs.getLong("chat_id");
                Long linkId = rs.getLong("id");
                String url = rs.getString("url");
                OffsetDateTime lastUpdatedAt = rs.getObject("last_updated_at", OffsetDateTime.class);

                Link link = new Link(
                        linkId,
                        url,
                        tagsRepository.findAllTagsByLinkId(linkId),
                        lastUpdatedAt != null ? lastUpdatedAt.toInstant() : null);
                map.computeIfAbsent(chatId, k -> new ArrayList<>()).add(link);
            }

            return map;
        });
    }

    private record LinksRow(Long id, String url, OffsetDateTime lastUpdatedAt) {}
}
