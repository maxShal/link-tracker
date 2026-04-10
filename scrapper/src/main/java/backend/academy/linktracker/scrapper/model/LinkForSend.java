package backend.academy.linktracker.scrapper.model;

import java.util.List;

public record LinkForSend(
        Long linkId,
        String url,
        List<Long> tgChatIds,
        String title,
        String author,
        String createdAt,
        String description) {}
