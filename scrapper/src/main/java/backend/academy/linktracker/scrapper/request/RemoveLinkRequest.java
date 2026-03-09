package backend.academy.linktracker.scrapper.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RemoveLinkRequest(
        @Pattern(regexp = "^(https)://.*$", message = "Некорректная ссылка") @NotBlank
        String link) {}
