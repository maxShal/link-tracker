package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.MetadataService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Comparator;
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
    private final BotClient botClient;
    private final BotMessageSender sender;
    private final SchedulerProperties properties;

    @Scheduled(fixedDelay = 10000)
    public void checkUpdates() {
        int page = properties.getPage();
        while (true) {
            var links = linksService.findAllForUpdateCheck(page, properties.getSize());
            if (links.isEmpty()) {
                break;
            }
            log.atInfo().addKeyValue("Scheduled", "Start").log("Scheduled check");

            for (LinkForUpdateCheck link : links) {
                try {
                    var response = linkMetadataService.getLastUpdated(link.url());

                    LinkUpdateResponse latestUpdate = response.stream()
                            .filter(item -> item.createdAt() != null)
                            .max(Comparator.comparing(item ->
                                    OffsetDateTime.parse(item.createdAt()).toInstant()))
                            .orElse(null);

                    if (latestUpdate == null) {
                        log.atInfo()
                                .addKeyValue("linkId", link.id())
                                .addKeyValue("url", link.url())
                                .log("No updates found");
                        continue;
                    }
                    Instant actualLastUpdated =
                            OffsetDateTime.parse(latestUpdate.createdAt()).toInstant();

                    if (link.lastUpdatedAt() == null || actualLastUpdated.isAfter(link.lastUpdatedAt())) {
                        linksService.updateLastUpdated(link.id(), actualLastUpdated);

                        sender.sendMessageToBot(latestUpdate, link);

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
            page++;
        }
    }
}
