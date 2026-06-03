package backend.academy.linktracker.ai.service;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.ai.configuration.properties.FilteringProperties;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilteringServiceTest {
    private final FilteringService filteringService =
            new FilteringService(new FilteringProperties(List.of("spam", "ads", "promo"), List.of("bot-user"), 20));

    @Test
    void shouldFilterByStopWord() {
        var update = raw("normal text with spam inside", "user");

        boolean result = filteringService.isApproveFilter(update);

        assertFalse(result);
    }

    @Test
    void shouldFilterByExcludedAuthor() {
        var update = raw("valid description with enough length", "bot-user");

        boolean result = filteringService.isApproveFilter(update);

        assertFalse(result);
    }

    @Test
    void shouldFilterByMinLength() {
        var update = raw("short", "user");

        boolean result = filteringService.isApproveFilter(update);

        assertFalse(result);
    }

    @Test
    void shouldApproveValidUpdate() {
        var update = raw("valid description with enough length", "user");

        boolean result = filteringService.isApproveFilter(update);

        assertTrue(result);
    }

    private RawLinkUpdate raw(String description, String author) {
        return new RawLinkUpdate(1L, "https://github.com/owner/repo", description, author, List.of(1L));
    }
}
