package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.AbstractPostgresContainerTest;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
/*import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;*/
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.repository-type=jdbc", "app.message-transport=http"})
@AutoConfigureMockMvc
public class JdbcBaseTest extends AbstractPostgresContainerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ITgChatRepository tgChatRepository;

    @Autowired
    protected ILinksRepository linksRepository;
/*
    @Autowired
    CacheManager cacheManager;*/

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.update("DELETE FROM link_tag");
        jdbcTemplate.update("DELETE FROM link_chat");
        jdbcTemplate.update("DELETE FROM tags");
        jdbcTemplate.update("DELETE FROM links");
        jdbcTemplate.update("DELETE FROM chats");

/*        Cache cache = cacheManager.getCache("links");
        if (cache != null) {
            cache.clear();
        }*/
    }
}
