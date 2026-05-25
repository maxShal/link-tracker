package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.configuration.properties.GithubProperties;
import backend.academy.linktracker.scrapper.configuration.properties.StackoverflowProperties;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@AllArgsConstructor
public class ExternalRestClientConfiguration {

    private final GithubProperties githubProperties;
    private final StackoverflowProperties stackoverflowProperties;

    @Bean
    public RestClient githubRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(githubProperties.getUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");

        if (githubProperties.getToken() != null && !githubProperties.getToken().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubProperties.getToken());
        }

        return builder.build();
    }

    @Bean
    public RestClient stackoverflowRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(stackoverflowProperties.getUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
