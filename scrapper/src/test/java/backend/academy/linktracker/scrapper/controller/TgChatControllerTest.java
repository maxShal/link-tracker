package backend.academy.linktracker.scrapper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ScrapperIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addAndGetLinkTest() throws Exception {

        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

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
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value("https://github.com/owner/repo"));
    }

    @Test
    void addAndDeleteAndGetLinkTest() throws Exception {
        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

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
                .andExpect(status().isOk());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.links").isEmpty());
    }

    @Test
    void deleteLinkWithoutChat() throws Exception {

        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

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
                .andExpect(status().isOk());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", 1999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value("https://github.com/owner/repo"));
    }

    @Test
    void addLinkWithoutChat() throws Exception {
        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

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
        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

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
}
