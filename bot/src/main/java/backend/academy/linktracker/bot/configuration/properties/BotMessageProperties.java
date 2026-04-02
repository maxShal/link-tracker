package backend.academy.linktracker.bot.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("app.messages")
public class BotMessageProperties {
    private String help;
    private String start;
    private String unknown;
    private String first;
    private String list;
    private String cancel;
    // Для track
    private String trackLink;
    private String trackTags;
    private String invalidLink;
    private String trackSuccess;
    private String linkAlreadyExists;

    private String untrackLink;
    private String untrackSuccess;

    private String linkNot;
}
