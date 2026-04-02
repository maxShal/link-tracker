package backend.academy.linktracker.scrapper.repository.jpa;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
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
class JpaTgChatsRepositoryTest {
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
