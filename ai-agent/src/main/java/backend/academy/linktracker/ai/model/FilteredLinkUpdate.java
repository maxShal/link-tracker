package backend.academy.linktracker.ai.model;

import java.util.List;

public record FilteredLinkUpdate(Long id, String url, String description, List<Long> tgChatIds, Priority priority) {}
