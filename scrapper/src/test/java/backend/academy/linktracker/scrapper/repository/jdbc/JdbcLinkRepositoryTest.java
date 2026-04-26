package backend.academy.linktracker.scrapper.repository.jdbc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@SpringBootTest(properties = {"app.repository-type=jdbc", "app.message-transport=http"})
@AutoConfigureMockMvc
@Testcontainers
class JdbcLinkRepositoryTest {
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
        jdbcTemplate.update("DELETE FROM link_tag");
        jdbcTemplate.update("DELETE FROM link_chat");
        jdbcTemplate.update("DELETE FROM tags");
        jdbcTemplate.update("DELETE FROM links");
        jdbcTemplate.update("DELETE FROM chats");
    }

    @Test
    void shouldReturnSaveLink() throws Exception {
        long chatId = 1L;

        tgChatRepository.saveChat(chatId);
        linksRepository.saveLink(
                chatId,
                new Link(
                        null,
                        "https://github.com/test/repo",
                        List.of("java", "spring"),
                        Instant.parse("2026-03-26T10:00:00Z")));

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value("https://github.com/test/repo"))
                .andExpect(jsonPath("$.links[0].tags[0]").value("java"));
    }

    @Test
    void shouldDeleteSaveLink() throws Exception {

        long chatId = 2L;
        String url = "https://github.com/test/repo";

        tgChatRepository.saveChat(chatId);
        linksRepository.saveLink(
                chatId, new Link(null, url, List.of("backend"), Instant.parse("2026-03-26T11:00:00Z")));

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType("application/json")
                        .content("""
                        {
                          "link": "https://github.com/test/repo"
                        }
                        """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links.length()").value(0));
    }

    @Test
    void shouldErrorThenLinkIsExcepted() throws Exception {

        long chatId = 2L;
        String url = "https://github.com/test/repo";

        tgChatRepository.saveChat(chatId);
        linksRepository.saveLink(
                chatId, new Link(null, url, List.of("backend"), Instant.parse("2026-03-26T11:00:00Z")));

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType("application/json")
                        .content("""
                {
                  "link": "https://github.com/test/repo"
                }
                """))
                .andExpect(status().isConflict());
    }
}
