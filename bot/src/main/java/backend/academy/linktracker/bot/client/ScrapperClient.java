package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.configuration.properties.ScrapperProperties;
import backend.academy.linktracker.bot.model.dto.request.AddLinkRequest;
import backend.academy.linktracker.bot.model.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.model.dto.response.LinkResponse;
import backend.academy.linktracker.bot.model.dto.response.ListLinksResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@AllArgsConstructor
@Component
public class ScrapperClient {
    private final RestClient scrapperRestClient;
    private final ScrapperProperties scrapperProperties;

    public void registerChat(long chatId) {
        scrapperRestClient
                .post()
                .uri(scrapperProperties.getChatEndpoint(), chatId)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean existChat(long chatId) {
        return Boolean.TRUE.equals(scrapperRestClient
                .get()
                .uri(scrapperProperties.getChatEndpoint(), chatId)
                .retrieve()
                .body(Boolean.class));
    }

    public ListLinksResponse getLinks(long chatId) {

        var request = scrapperRestClient.get().uri(scrapperProperties.getLinkEndpoint());
        return request.header(scrapperProperties.getHeader(), String.valueOf(chatId))
                .retrieve()
                .body(ListLinksResponse.class);
    }

    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        return scrapperRestClient
                .post()
                .uri(scrapperProperties.getLinkEndpoint())
                .header(scrapperProperties.getHeader(), String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }

    public LinkResponse deleteLink(long chatId, RemoveLinkRequest request) {
        return scrapperRestClient
                .method(HttpMethod.DELETE)
                .uri(scrapperProperties.getLinkEndpoint())
                .header(scrapperProperties.getHeader(), String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }
}
