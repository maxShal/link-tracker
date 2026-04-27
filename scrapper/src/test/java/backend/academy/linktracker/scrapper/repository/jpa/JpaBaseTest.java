package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(properties = {"app.repository-type=jpa", "app.message-transport=http"})
@AutoConfigureMockMvc
@Testcontainers
public class JpaBaseTest {
    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("newTest")
            .withPassword("newTest")
            .withUsername("newTest");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ITgChatRepository tgChatRepository;

    @Autowired
    protected ILinksRepository linksRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

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
    }
}
