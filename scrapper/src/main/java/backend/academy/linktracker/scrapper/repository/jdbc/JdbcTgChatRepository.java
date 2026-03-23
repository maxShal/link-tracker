package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.repository-type", havingValue = "jdbc")
public class JdbcTgChatRepository implements ITgChatRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveChat(Long id) {
        jdbcTemplate.update("""
        INSERT INTO chats (id)  VALUES (?)
        ON CONFLICT (id) DO NOTHING
        """, id);
    }

    @Override
    public boolean existsChats(Long id) {
        Integer count = jdbcTemplate.queryForObject("""
        SELECT COUNT(*) FROM chats WHERE id = ?
        """, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void deleteChat(Long id) {
        jdbcTemplate.update("""
        DELETE FROM chats WHERE id = ?
        """, id);
    }
}
