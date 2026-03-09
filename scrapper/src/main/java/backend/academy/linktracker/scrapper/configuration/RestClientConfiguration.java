package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.properties.BotProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@AllArgsConstructor
public class RestClientConfiguration {
    private final BotProperties botProperties;

    @Bean
    public RestClient botRestClient() {
        return RestClient.builder().baseUrl(botProperties.getUrl()).build();
    }
}
