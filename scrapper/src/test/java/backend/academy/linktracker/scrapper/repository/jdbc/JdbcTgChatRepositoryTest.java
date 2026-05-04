package backend.academy.linktracker.scrapper.repository.jdbc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class JdbcTgChatRepositoryTest extends JdbcBaseTest {

    @Test
    void shouldReturnSaveTgChat() throws Exception {
        long chatId = 1L;
        tgChatRepository.saveChat(chatId);

        mockMvc.perform(get("/tg-chat/1")).andExpect(status().isOk());
    }

    @Test
    void shouldReturnNoTgChat() throws Exception {
        long chatId = 1L;
        tgChatRepository.saveChat(chatId);

        mockMvc.perform(delete("/tg-chat/1")).andExpect(status().isOk());

        assertFalse(tgChatRepository.existsChats(1L));
    }
}
