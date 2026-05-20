package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.AbstractPostgresContainerTest;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.repository-type=jpa", "app.message-transport=http"})
@AutoConfigureMockMvc
public class JpaBaseTest extends AbstractPostgresContainerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ITgChatRepository tgChatRepository;

    @Autowired
    protected ILinksRepository linksRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.execute("""
            TRUNCATE TABLE
                link_tag,
                link_chat,
                tags,
                links,
                chats
            RESTART IDENTITY CASCADE
        """);

        Cache cache = cacheManager.getCache("links");
        if (cache != null) {
            cache.clear();
        }
    }
}
