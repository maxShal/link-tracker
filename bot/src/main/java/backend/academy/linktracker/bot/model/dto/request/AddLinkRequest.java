package backend.academy.linktracker.bot.model.dto.request;

import java.util.List;

public record AddLinkRequest(String link, List<String> tags) {}
