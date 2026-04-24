package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.configuration.properties.SchedulerProperties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerExecutorConfiguration {
    @Bean(destroyMethod = "shutdown")
    public ExecutorService linkUpdateExecutor(SchedulerProperties properties) {
        return Executors.newFixedThreadPool(properties.getThreads());
    }
}
