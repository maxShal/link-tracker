package backend.academy.linktracker.scrapper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ScrapperIntegrationTest {

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
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.update("DELETE FROM link_chat");
        jdbcTemplate.update("DELETE FROM links");
        jdbcTemplate.update("DELETE FROM chats");
    }

    @Test
    void addAndGetLinkTest() throws Exception {

        registerChat(1L);
        addLink(1L, DEFAULT_LINK);

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value(DEFAULT_LINK));
    }
/*
    @Test
    void addAndDeleteAndGetLinkTest() throws Exception {
        registerChat(1L);

        addLink(1L, DEFAULT_LINK);
        deleteLink(1L, DEFAULT_LINK, status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.links").isEmpty());
    }*/

    @Test
    void deleteLinkWithoutChat() throws Exception {

        registerChat(1L);
        addLink(1L, DEFAULT_LINK);

        deleteLink(1999L, DEFAULT_LINK, status().isNotFound());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value(DEFAULT_LINK));
    }

    @Test
    void addLinkWithoutChat() throws Exception {
        registerChat(1L);

        String body = """
            {
            "link": "https://github.com/owner/repo",
            "tags": ["tags"]
            }
        """;

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 1999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void addLinkInDeleteChat() throws Exception {
        registerChat(1L);

        mockMvc.perform(delete("/tg-chat/1")).andExpect(status().isOk());

        String body = """
            {
            "link": "https://github.com/owner/repo",
            "tags": ["tags"]
            }
        """;

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWithoutChat() throws Exception {
        mockMvc.perform(delete("/tg-chat/1")).andExpect(status().isNotFound());
    }

    private void registerChat(long chatId) throws Exception {
        mockMvc.perform(post("/tg-chat/" + chatId)).andExpect(status().isOk());
    }

    private void addLink(long chatId, String link) throws Exception {
        String body = """
        {
          "link": "%s",
          "tags": ["tags"]
        }
        """.formatted(link);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private void deleteLink(long chatId, String link, ResultMatcher status) throws Exception {
        String body = """
        { "link": "%s" }
        """.formatted(link);

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status);
    }

    private static final String DEFAULT_LINK = "https://github.com/owner/repo";
}
