package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.properties.GroupProperties;
import backend.academy.linktracker.ai.configuration.properties.KafkaProducerProperties;
import backend.academy.linktracker.ai.model.FilteredLinkUpdate;
import backend.academy.linktracker.ai.model.Priority;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final KafkaTemplate<String, FilteredLinkUpdate> kafkaTemplate;
    private final KafkaProducerProperties kafkaProducerProperties;
    private final GroupProperties groupingProperties;

    private final Map<Long, List<FilteredLinkUpdate>> buffer = new ConcurrentHashMap<>();
    private final Set<Long> scheduledChats = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public void add(FilteredLinkUpdate update) {
        for (Long chatId : update.tgChatIds()) {
            buffer.computeIfAbsent(chatId, ignored -> new CopyOnWriteArrayList<>())
                    .add(update);

            if (scheduledChats.add(chatId)) {
                scheduler.schedule(() -> flush(chatId), groupingProperties.windowMs(), TimeUnit.MILLISECONDS);
            }
        }
    }

    private void flush(Long chatId) {
        try {
            List<FilteredLinkUpdate> updates = buffer.remove(chatId);
            scheduledChats.remove(chatId);

            if (updates == null || updates.isEmpty()) {
                return;
            }

            FilteredLinkUpdate result =
                    updates.size() == 1 ? single(chatId, updates.getFirst()) : grouped(chatId, updates);

            kafkaTemplate.send(kafkaProducerProperties.getTopic(), result);

            log.atInfo().addKeyValue("id", result.id()).log("Отправил update: {}", result);

        } catch (Exception e) {
            log.error("Ошибка обработки raw update", e);
        }
    }

    private FilteredLinkUpdate single(Long chatId, FilteredLinkUpdate update) {
        return new FilteredLinkUpdate(
                update.id(), update.url(), update.description(), List.of(chatId), update.priority());
    }

    private FilteredLinkUpdate grouped(Long chatId, List<FilteredLinkUpdate> updates) {
        String description = IntStream.range(0, updates.size())
                .mapToObj(i -> (i + 1) + ". " + updates.get(i).description())
                .collect(Collectors.joining("\n"));

        Priority priority = maxPriority(updates);

        return new FilteredLinkUpdate(
                updates.getFirst().id(), updates.getFirst().url(), description, List.of(chatId), priority);
    }

    private Priority maxPriority(List<FilteredLinkUpdate> updates) {
        if (updates.stream().anyMatch(update -> Priority.HIGH == update.priority())) {
            return Priority.HIGH;
        }

        if (updates.stream().anyMatch(update -> Priority.MEDIUM == update.priority())) {
            return Priority.MEDIUM;
        }

        return Priority.LOW;
    }
}
