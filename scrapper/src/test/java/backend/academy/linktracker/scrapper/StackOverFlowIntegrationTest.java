package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.model.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.model.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.model.response.LinkResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class StackOverFlowIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "https://stackoverflow.com/questions/123/test-title";

    @Test
    void shouldSendNewAnswer() throws InterruptedException {
        long chatId = 1L;
        tgChatService.addChat(chatId);

        LinkResponse linkResponse = linksService.addLink(chatId, new AddLinkRequest(URL, List.of()));
        linksService.updateLastUpdated(linkResponse.id(), Instant.parse("2020-01-01T00:00:00Z"));

        wireMock.stubFor(get(urlPathEqualTo("/2.3/questions/123"))
                .withQueryParam("site", equalTo("ru.stackoverflow"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                {
                  "items": [
                    {
                      "question_id": 123,
                      "title": "title"
                    }
                  ]
                }
                """)));

        wireMock.stubFor(get(urlPathEqualTo("/2.3/questions/123/answers"))
                .withQueryParam("site", equalTo("ru.stackoverflow"))
                .withQueryParam("filter", equalTo("withbody"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                      "items": [
                        {
                          "owner": {
                            "display_name": "hatirlatici"
                          },
                          "creation_date": 1767579672,
                          "answer_id": 79860452,
                          "body": "it is body"
                        }
                      ]
                    }
                    """)));

        wireMock.stubFor(get(urlPathEqualTo("/2.3/questions/123/comments"))
                .withQueryParam("site", equalTo("ru.stackoverflow"))
                .withQueryParam("filter", equalTo("withbody"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "items": []
                            }
                            """)));

        scheduler.checkUpdates();

        ArgumentCaptor<LinkUpdateRequest> captor = ArgumentCaptor.forClass(LinkUpdateRequest.class);

        verify(sender).sendUpdate(captor.capture());

        LinkUpdateRequest request = captor.getValue();
        assertTrue(request.description().contains("title"));
        assertTrue(request.description().contains("hatirlatici"));
        assertTrue(request.description().contains("it is body"));
    }
}
