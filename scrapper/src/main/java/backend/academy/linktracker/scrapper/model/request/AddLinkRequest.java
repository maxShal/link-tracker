package backend.academy.linktracker.scrapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record AddLinkRequest(
        @Pattern(regexp = "^(https?)://.*$", message = "Некорректная ссылка") @NotBlank
        String link,

        List<String> tags) {}
