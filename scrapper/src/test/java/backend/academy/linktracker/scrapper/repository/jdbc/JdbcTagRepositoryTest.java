package backend.academy.linktracker.scrapper.repository.jdbc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.model.Link;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class JdbcTagRepositoryTest extends JdbcBaseTest {

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
