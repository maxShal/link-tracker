package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.properties.PrioritizationProperties;
import backend.academy.linktracker.ai.model.Priority;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizationServiceTest {

    PrioritizationService prioritizationService = new PrioritizationService(
        new PrioritizationProperties(
        List.of("critical"),
        List.of("minor"))
    );

    @Test
    void shouldReturnHighWhenHighKeyWords()
    {
        Priority priority = prioritizationService.getPriority("critical");

        assertEquals(Priority.HIGH, priority);
    }
    @Test
    void shouldReturnLowWhenLowKeyWords()
    {
        Priority priority = prioritizationService.getPriority("minor");

        assertEquals(Priority.LOW, priority);
    }

    @Test
    void shouldReturnMediumWhenNoKeyWords()
    {
        Priority priority = prioritizationService.getPriority("-");

        assertEquals(Priority.MEDIUM, priority);
    }



}
