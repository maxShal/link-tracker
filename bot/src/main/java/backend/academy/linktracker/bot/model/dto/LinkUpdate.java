package backend.academy.linktracker.bot.model.dto;

import backend.academy.linktracker.bot.util.Utils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record LinkUpdate(
        @NotNull Long id,
        @NotBlank @Pattern(regexp = Utils.PARAM) String url,
        @NotBlank String description,
        @NotEmpty List<@NotNull Long> tgChatIds) {}
