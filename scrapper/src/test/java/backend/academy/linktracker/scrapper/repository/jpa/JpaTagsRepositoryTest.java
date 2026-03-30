package backend.academy.linktracker.scrapper.repository.jpa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@SpringBootTest(properties = "app.repository-type=jpa")
@AutoConfigureMockMvc
@Testcontainers
class JpaTagsRepositoryTest {

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("test")
            .withPassword("test")
            .withUsername("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ITgChatRepository tgChatRepository;

    @Autowired
    private ILinksRepository linksRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

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

    @Test
    void shouldReturnSaveTags() throws Exception {
        long chatId = 1L;
        String url = "https://github.com/test/repo";
        tgChatRepository.saveChat(chatId);
        linksRepository.saveLink(chatId, new Link(null, url, List.of("java"), Instant.parse("2026-03-26T10:00:00Z")));

        mockMvc.perform(get("/tags").header("Tg-Chat-Id", chatId).param("url", url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0]").value("java"));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        long chatId = 1L;
        String url = "https://github.com/test/repo";

        tgChatRepository.saveChat(chatId);
        linksRepository.saveLink(chatId, new Link(null, url, List.of("java"), Instant.parse("2026-03-26T10:00:00Z")));

        mockMvc.perform(delete("/tags")
                        .header("Tg-Chat-Id", chatId)
                        .contentType("application/json")
                        .content("""
                    {
                      "url": "https://github.com/test/repo",
                      "tag": "java"
                    }
                    """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tags").header("Tg-Chat-Id", chatId).param("url", url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags.length()").value(0));
    }
}
