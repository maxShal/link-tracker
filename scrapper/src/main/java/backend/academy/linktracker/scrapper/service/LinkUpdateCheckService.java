package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdateCheckService {

    private final LinksService linksService;
    private final MetadataService linkMetadataService;

    @Transactional
    public LinkForSend processLinkCheck(LinkForUpdateCheck link) {
        var response = linkMetadataService.getLastUpdated(link.url());

        if (response.isEmpty()) {
            log.atInfo()
                    .addKeyValue("linkId", link.id())
                    .addKeyValue("url", link.url())
                    .log("No response found");
        }

        LinkUpdateResponse latestUpdate = response.stream()
                .filter(updateResponse -> updateResponse.createdAt() != null)
                .max(Comparator.comparing(updateResponse ->
                        OffsetDateTime.parse(updateResponse.createdAt()).toInstant()))
                .orElseThrow(() -> new NoSuchElementException("No response found"));

        Instant actualLastUpdated =
                OffsetDateTime.parse(latestUpdate.createdAt()).toInstant();

        if (link.lastUpdatedAt() == null || actualLastUpdated.isAfter(link.lastUpdatedAt())) {
            linksService.updateLastUpdated(link.id(), actualLastUpdated);
            LinkForSend linkForSend = new LinkForSend(
                    link.id(),
                    link.url(),
                    latestUpdate.title(),
                    latestUpdate.author(),
                    latestUpdate.createdAt(),
                    latestUpdate.description(),
                    link.tgChatIds());
            log.atInfo()
                    .addKeyValue("linkId", link.id())
                    .addKeyValue("url", link.url())
                    .log("Link update checked");

            return linkForSend;
        }
        return null;
    }
}
