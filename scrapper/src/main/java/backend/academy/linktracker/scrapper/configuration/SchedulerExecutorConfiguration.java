package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class SchedulerExecutorConfiguration {
    @Bean
    public ExecutorService linkUpdateExecutor(SchedulerProperties properties)
    {
        return  Executors.newFixedThreadPool(properties.getThreads());
    }
}
