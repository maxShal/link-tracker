package backend.academy.linktracker.ai.service;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.ai.configuration.properties.GroupProperties;
import backend.academy.linktracker.ai.configuration.properties.KafkaProducerProperties;
import backend.academy.linktracker.ai.model.FilteredLinkUpdate;
import java.time.Duration;
import java.util.List;
import backend.academy.linktracker.ai.model.Priority;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

class GroupServiceTest {

    private final KafkaTemplate<String, FilteredLinkUpdate> kafkaTemplate = mock(KafkaTemplate.class);

    private final KafkaProducerProperties kafkaProducerProperties = createKafkaProperties();


    private final GroupProperties groupingProperties = new GroupProperties(100);

    private final GroupService groupService = new GroupService(
        kafkaTemplate,
        kafkaProducerProperties,
        groupingProperties
    );

    @Test
    void shouldGroupSeveralUpdatesForSameChatId() {
        var first = new FilteredLinkUpdate(
            1L,
            "https://github.com/owner/repo",
            "First update",
            List.of(1L),
            Priority.LOW
        );

        var second = new FilteredLinkUpdate(
            2L,
            "https://github.com/owner/repo",
            "Second update",
            List.of(1L),
            Priority.HIGH
        );

        groupService.add(first);
        groupService.add(second);

        ArgumentCaptor<FilteredLinkUpdate> captor = ArgumentCaptor.forClass(FilteredLinkUpdate.class);

        await()
            .atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> verify(kafkaTemplate).send(
                eq("link.processed-updates"),
                captor.capture()
            ));

        FilteredLinkUpdate result = captor.getValue();

        assert result.description().contains("1. First update");
        assert result.description().contains("2. Second update");
        assert result.tgChatIds().equals(List.of(1L));
        assert result.priority().equals(Priority.HIGH);
    }

    @Test
    void shouldSendSingleUpdateWithoutGrouping() {
        var update = new FilteredLinkUpdate(
            1L,
            "https://github.com/owner/repo",
            "Only one update",
            List.of(1L),
            Priority.MEDIUM
        );

        groupService.add(update);

        ArgumentCaptor<FilteredLinkUpdate> captor = ArgumentCaptor.forClass(FilteredLinkUpdate.class);

        await()
            .atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> verify(kafkaTemplate).send(
                eq("link.processed-updates"),
                captor.capture()
            ));

        FilteredLinkUpdate result = captor.getValue();

        assert result.id().equals(1L);
        assert result.description().equals("Only one update");
        assert result.tgChatIds().equals(List.of(1L));
        assert result.priority().equals(Priority.MEDIUM);
    }

    private KafkaProducerProperties createKafkaProperties() {
        KafkaProducerProperties properties = new KafkaProducerProperties();

        properties.setTopic("link.processed-updates");
        properties.setPartitions(1);
        properties.setReplicas((short) 1);

        return properties;
    }
}

