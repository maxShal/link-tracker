package backend.academy.linktracker.scrapper.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class LinkTrackerMetrics {

    private final Counter linksAdded;
    private final Counter linksDeleted;
    private final Counter linkUpdatesDetected;

    public LinkTrackerMetrics(MeterRegistry registry) {
        linksAdded = Counter.builder("linktracker.links.added")
                .description("Successfully added link subscription")
                .register(registry);
        linksDeleted = Counter.builder("linktracker.links.deleted")
                .description("Successfully deleted link subscriptions")
                .register(registry);
        linkUpdatesDetected = Counter.builder("linktracker.link.updates.detected")
                .description("New updates detected for tracked links")
                .register(registry);
    }

    public void linkAdded() {
        linksAdded.increment();
    }

    public void linkDeleted() {
        linksDeleted.increment();
    }

    public void linkUpdateDetected() {
        linkUpdatesDetected.increment();
    }
}
