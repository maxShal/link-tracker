package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.configuration.properties.BotProperties;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
public class BotClient {
    private final RestClient botRestClient;

    private final BotProperties botProperties;

    public void sendUpdate(LinkUpdateRequest request) {
        botRestClient
                .post()
                .uri(botProperties.getUpdateEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
