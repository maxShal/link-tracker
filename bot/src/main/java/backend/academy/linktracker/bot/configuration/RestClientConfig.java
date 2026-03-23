package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.ScrapperProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@AllArgsConstructor
public class RestClientConfig {
    private final ScrapperProperties scrapperProperties;

    @Bean
    public RestClient scrapperRestClient() {
        return RestClient.builder().baseUrl(scrapperProperties.getUrl()).build();
    }
}
