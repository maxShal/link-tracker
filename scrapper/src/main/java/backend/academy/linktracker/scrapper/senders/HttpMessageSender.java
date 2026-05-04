package backend.academy.linktracker.scrapper.senders;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.model.LinkForSend;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "http")
public class HttpMessageSender implements ISendUpdate {

    private final BotClient botClient;

    private final MassageForSendMaker massageForSendMaker;

    public void send(LinkForSend linkForSend) {

        botClient.sendUpdate(massageForSendMaker.linkForSend(linkForSend));
    }
}
