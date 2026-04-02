package backend.academy.linktracker.scrapper.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.scheduler")
public class SchedulerProperties {
    private int page;
    private int size;
}
