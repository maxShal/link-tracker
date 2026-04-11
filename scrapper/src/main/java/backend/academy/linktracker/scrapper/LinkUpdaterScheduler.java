package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import backend.academy.linktracker.scrapper.senders.HttpMessageSender;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.MetadataService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class LinkUpdaterScheduler {

    private final LinksService linksService;
    private final MetadataService linkMetadataService;
    private final HttpMessageSender sender;
    private final SchedulerProperties properties;
    private final ExecutorService linkUpdateExecutor;

    @Scheduled(fixedDelayString = "${app.scheduler.check}")
    public void checkUpdates() {
        int page = properties.getPage();
        while (true) {
            var links = linksService.findAllForUpdateCheck(page, properties.getSize());
            if (links.isEmpty()) {
                break;
            }

            int threads = properties.getThreads();
            int chunkSize = (int) Math.max(1, ((double) links.size() / threads));
            log.atInfo().addKeyValue("Scheduled", "Start").log("Scheduled check");

            List<List<LinkForUpdateCheck>> partitions = partition(links, chunkSize);

            List<Callable<Void>> tasks = partitions.stream()
                    .map(part -> (Callable<Void>) () -> {
                        for (LinkForUpdateCheck link : part) {
                            processLink(link);
                        }
                        return null;
                    })
                    .toList();

            try {
                List<Future<Void>> futures = linkUpdateExecutor.invokeAll(tasks);
                for (Future<Void> future : futures) {
                    future.get();
                }
            } catch (Exception e) {
                log.atError().log("Thread error", e);
            }
            page++;
        }
    }

    private void processLink(LinkForUpdateCheck link) {
        try {
            var response = linkMetadataService.getLastUpdated(link.url());

            if (response.isEmpty()) {
                log.atInfo()
                        .addKeyValue("linkId", link.id())
                        .addKeyValue("url", link.url())
                        .log("No response found");
            }

            LinkUpdateResponse latestUpdate = null;
            if (response.isPresent()) {
                latestUpdate = response.get();
            }
            /*            LinkUpdateResponse latestUpdate = response.stream()
            .filter(r -> r.createdAt()!= null)
            .max(Comparator.comparing(r))
            ;*/

            Instant actualLastUpdated =
                    OffsetDateTime.parse(latestUpdate.createdAt()).toInstant();

            if (link.lastUpdatedAt() == null || actualLastUpdated.isAfter(link.lastUpdatedAt())) {
                linksService.updateLastUpdated(link.id(), actualLastUpdated);
                LinkForSend linkForSend = new LinkForSend(
                        link.id(),
                        link.url(),
                        link.tgChatIds(),
                        latestUpdate.title(),
                        latestUpdate.author(),
                        latestUpdate.createdAt(),
                        latestUpdate.description());
                sender.send(linkForSend);

                log.atInfo()
                        .addKeyValue("linkId", link.id())
                        .addKeyValue("url", link.url())
                        .log("Update sent");
            }

        } catch (Exception e) {
            log.atError()
                    .addKeyValue("linkId", link.id())
                    .addKeyValue("url", link.url())
                    .log("Failed to check link update", e);
        }
    }

    private List<List<LinkForUpdateCheck>> partition(List<LinkForUpdateCheck> links, int chunkSize) {
        List<List<LinkForUpdateCheck>> result = new ArrayList<>();
        for (int i = 0; i < links.size(); i += chunkSize) {
            result.add(links.subList(i, Math.min(i + chunkSize, links.size())));
        }
        return result;
    }
}
