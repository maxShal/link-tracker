package backend.academy.linktracker.scrapper.repository.jpa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.model.Link;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class JpaLinkRepositoryTest extends JpaBaseTest {

    @Test
    void shouldReturnSaveLink() throws Exception {
        long chatId = 1L;

        tgChatRepository.saveChat(chatId);
        linksRepository.saveLink(
                chatId,
                new Link(null, "https://github.com/test/repo", List.of("java"), Instant.parse("2026-03-26T10:00:00Z")));

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
