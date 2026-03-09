package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.request.AddLinkRequest;
import backend.academy.linktracker.bot.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.dto.response.LinkResponse;
import backend.academy.linktracker.bot.dto.response.ListLinksResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@AllArgsConstructor
@Component
public class ScrapperClient {
    private final RestClient scrapperRestClient;

    public void registerChat(long chatId) {
        scrapperRestClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    public ListLinksResponse getLinks(long chatId) {

        var request = scrapperRestClient.get().uri("/links");
        return request.header("Tg-Chat-Id", String.valueOf(chatId)).retrieve().body(ListLinksResponse.class);
    }

    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        return scrapperRestClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }

    public LinkResponse deleteLink(long chatId, RemoveLinkRequest request) {
        return scrapperRestClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }
}
