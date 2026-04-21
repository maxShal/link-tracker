package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.model.LinkForSend;
import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.senders.ISendUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdateSendService {

    private final LinkUpdateCheckService linkUpdateCheckService;
    private final ISendUpdate sendUpdate;

    public void processLinkSen(LinkForUpdateCheck linkForUpdateCheck) {
        try {
            LinkForSend linkForSend = linkUpdateCheckService.processLinkCheck(linkForUpdateCheck);
            if (linkForSend != null) {
                sendUpdate.send(linkForSend);
                log.atInfo()
                        .addKeyValue("linkId", linkForSend.linkId())
                        .addKeyValue("url", linkForSend.url())
                        .log("Link update send");
            }
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("linkId", linkForUpdateCheck.id())
                    .addKeyValue("url", linkForUpdateCheck.url())
                    .log("Failed to check link update", e);
        }
    }
}
