package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.service.MetadataService;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class LinkUpdaterScheduler {

    private final ILinksRepository linksRepository;
    private final MetadataService linkMetadataService;
    private final BotClient botClient;

    @Scheduled(fixedDelay = 10000)
    public void checkUpdates() {
        var links = linksRepository.findAllForUpdateCheck();
        log.atInfo()
            .addKeyValue("Scheduled", "Start")
            .log("Scheduled check");

        for (LinkForUpdateCheck link : links) {
            try {
                Instant actualLastUpdated = linkMetadataService.getLastUpdated(link.url());

                if (link.lastUpdatedAt() == null || actualLastUpdated.isAfter(link.lastUpdatedAt())) {
                    linksRepository.updateLastUpdated(link.id(), actualLastUpdated);

                    botClient.sendUpdate(
                            new LinkUpdateRequest(link.id(), link.url(), "Обнаружено обновление", link.tgChatIds()));

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
    }
}
