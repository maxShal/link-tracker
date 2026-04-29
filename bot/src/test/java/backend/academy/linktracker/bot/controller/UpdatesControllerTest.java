package backend.academy.linktracker.bot.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.bot.exception.BotExceptionHandler;
import backend.academy.linktracker.bot.service.TelegramBotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UpdatesController.class, properties = "app.message-transport=http")
@Import(BotExceptionHandler.class)
class UpdatesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TelegramBotService telegramBotService;

    @Test
    void goodPostUpdate() throws Exception {
        String request = """
       {
        "id": 1,
        "url": "https://github.com",
        "description": "GitHub link",
        "tgChatIds": [2]
       }
       """;

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk());
    }

    @Test
    void badPostUpdate() throws Exception {
        String request = """
       {
        "id": 1,
        "url": "incorrect link",
        "description": "GitHub link",
        "tgChatIds": [2]
       }
       """;

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());
    }
}
