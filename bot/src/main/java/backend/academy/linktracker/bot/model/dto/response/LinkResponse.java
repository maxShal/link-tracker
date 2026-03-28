package backend.academy.linktracker.bot.model.dto.response;

import java.util.List;

public record LinkResponse(Long id, String url, List<String> tags) {}
