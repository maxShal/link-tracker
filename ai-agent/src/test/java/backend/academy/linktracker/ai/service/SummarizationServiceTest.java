package backend.academy.linktracker.ai.service;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.ai.configuration.properties.SummarizationProperties;
import org.junit.jupiter.api.Test;

class SummarizationServiceTest {
    private final SummarizationService summarizationService = new SummarizationService(new SummarizationProperties(10));

    @Test
    void shouldSummarizeLongText() {
        String result = summarizationService.summarization("123456789012345");

        assertEquals("1234567890...", result);
    }

    @Test
    void shouldNotSummarizeShortText() {
        String result = summarizationService.summarization("short");

        assertEquals("short", result);
    }
}
