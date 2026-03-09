package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@AllArgsConstructor
public class ExternalRestClientConfiguration {

    private final GithubProperties githubProperties;
    private final StackoverflowProperties stackoverflowProperties;

    @Bean
    public RestClient githubRestClient() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.github.com");

        if (githubProperties.getToken() != null && !githubProperties.getToken().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubProperties.getToken());
        }

        return builder.build();
    }

    @Bean
    public RestClient stackoverflowRestClient() {
        return RestClient.builder().baseUrl("https://api.stackexchange.com").build();
    }
}
