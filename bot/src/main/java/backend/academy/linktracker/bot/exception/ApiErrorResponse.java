package backend.academy.linktracker.bot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ApiErrorResponse(
    @NotBlank String description,
    @NotBlank String code,
    @NotBlank String exceptionName,
    @NotBlank String exceptionMessage,
    @NotEmpty @Valid List<@NotBlank String> stacktrace
) {}
