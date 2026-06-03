package backend.academy.linktracker.ai.model;

import java.util.List;

public record RawLinkUpdate(Long id, String url, String description, String author, List<Long> tgChatIds) {}
