package backend.academy.linktracker.scrapper.senders;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class MassageForSendMakerTest {
    private final MassageForSendMaker maker = new MassageForSendMaker();

    @Test
    void shouldMakeShortMessage() {
        String longText = "a".repeat(250);

        LinkForSend linkForSend = new LinkForSend(
                1L, "https://github.com/owner/repo", "title", "author", "2026-04-07T10:00:00Z", longText, List.of(1L));

        LinkUpdateRequest request = maker.linkForSend(linkForSend);

        assertTrue(request.description().contains("a".repeat(200)));
        assertFalse(request.description().contains("a".repeat(201)));
        assertTrue(request.description().contains("title"));
        assertTrue(request.description().contains("author"));
    }
}
