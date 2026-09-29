package backend.academy.linktracker.scrapper.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.service.LinksService;
/*
import backend.academy.linktracker.scrapper.service.cache.ClientSideCachingService;
*/
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
/*import org.springframework.cache.CacheManager;*/
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SpringBootTest(
        classes = LinksControllerRetryTest.TestApplication.class,
        properties = {
            "resilience4j.ratelimiter.instances.getLinks.limitForPeriod=5",
            "resilience4j.ratelimiter.instances.getLinks.limitRefreshPeriod=5s",
            "resilience4j.ratelimiter.instances.getLinks.timeoutDuration=0s"
        })
@AutoConfigureMockMvc
class LinksControllerRetryTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinksService linksService;

/*    @MockitoBean
    private CacheManager cacheManager;*/


/*
    @MockitoBean
    private ClientSideCachingService clientSideCachingService;*/

    @Test
    void shouldReturn429WhenRateLimitExceeded() throws Exception {
        when(linksService.getAllLinks(anyLong(), anyInt(), anyInt()))
                .thenReturn(null);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());
        }

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isTooManyRequests());
    }

    @Configuration
    @EnableAutoConfiguration(
            exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                LiquibaseAutoConfiguration.class
            })
    @Import({LinksController.class, RateLimiterExceptionHandler.class})
    static class TestApplication {}

    @RestControllerAdvice
    static class RateLimiterExceptionHandler {

        @ExceptionHandler(RequestNotPermitted.class)
        ResponseEntity<Void> handleRateLimiterException() {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}
