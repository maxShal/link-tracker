package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.service.LinkUpdateSendService;
import backend.academy.linktracker.scrapper.service.LinksService;
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
    private final LinkUpdateSendService linkUpdateSendService;
    private final SchedulerProperties properties;
    private final ExecutorService linkUpdateExecutor;

    @Scheduled(fixedDelayString = "${app.scheduler.check}")
    public void checkUpdates() throws InterruptedException {
        int page = properties.getPage();
        while (true) {
            var links = linksService.findAllForUpdateCheck(page, properties.getSize());
            if (links.isEmpty()) {
                break;
            }
            int threads = properties.getThreads();
            int chunkSize = (int) Math.max(1, (double) links.size() / threads);
            log.atInfo().addKeyValue("Scheduled", "Start").log("Scheduled check");

            List<List<LinkForUpdateCheck>> partitions = partition(links, chunkSize);

            List<Callable<Void>> tasks = partitions.stream()
                    .map(part -> (Callable<Void>) () -> {
                        for (LinkForUpdateCheck link : part) {
                            linkUpdateSendService.processLinkSen(link);
                        }
                        return null;
                    })
                    .toList();

            List<Throwable> errors = new ArrayList<>();
            List<Future<Void>> futures = linkUpdateExecutor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    errors.add(e.getCause());
                }
            }

            page++;

            if (!errors.isEmpty()) {
                for (Throwable error : errors) {
                    log.atError().addKeyValue("error", error.getMessage()).log("Failed to check link update", error);
                }
            }
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
