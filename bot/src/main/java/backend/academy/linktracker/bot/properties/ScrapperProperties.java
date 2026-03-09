package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "app.scrapper")
public class ScrapperProperties {
    @NotBlank
    String url;
}
