package backend.academy.linktracker.scrapper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.repository.LinksRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

@SpringBootTest
@AutoConfigureMockMvc
class ScrapperIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TgChatRepository tgChatRepository;

    @Autowired
    private LinksRepository linksRepository;

    @BeforeEach
    void clearStage() {
        tgChatRepository.clear();
        linksRepository.clear();
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

        addLink(1L, DEFAULT_LINK);
        deleteLink(1L, DEFAULT_LINK, status().isOk());

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
            "tags": ["tags"],
            "filters": ["f1"]
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
            "tags": ["tags"],
            "filters": ["f1"]
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
          "tags": ["tags"],
          "filters": ["f1"]
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
