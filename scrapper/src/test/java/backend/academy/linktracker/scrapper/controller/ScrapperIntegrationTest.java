package backend.academy.linktracker.scrapper.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.AbstractPostgresContainerTest;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaChatsLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaLinksRepository;
import backend.academy.linktracker.scrapper.repository.jpa.intarfaces.IJpaTgChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

@AutoConfigureMockMvc
@SpringBootTest(properties = "app.message-transport=http")
class ScrapperIntegrationTest extends AbstractPostgresContainerTest {

    @Autowired
    private IJpaChatsLinksRepository jpaChatsLinksRepository;

    @Autowired
    private IJpaTgChatRepository jpaTgChatsRepository;

    @Autowired
    private IJpaLinksRepository jpaLinksRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.update("DELETE FROM link_tag");
        jdbcTemplate.update("DELETE FROM link_chat");
        jdbcTemplate.update("DELETE FROM tags");
        jdbcTemplate.update("DELETE FROM links");
        jdbcTemplate.update("DELETE FROM chats");

        Cache cache = cacheManager.getCache("links");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void addAndGetLinkTest() throws Exception {

        registerChat(1L);
        addLink(1L, DEFAULT_LINK);

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value(DEFAULT_LINK));
    }

    @Test
    void addAndDeleteAndGetLinkTest() throws Exception {
        registerChat(1L);
        assertTrue(jpaTgChatsRepository.existsById(1L));
        addLink(1L, DEFAULT_LINK);
        assertTrue(jpaLinksRepository.findByUrl(DEFAULT_LINK).isPresent());
        deleteLink(1L, DEFAULT_LINK, status().isOk());
        assertTrue(jpaLinksRepository.findByUrl(DEFAULT_LINK).isEmpty());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.links").isEmpty());
    }

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
