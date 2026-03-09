package backend.academy.linktracker.scrapper.model;

import java.time.Instant;
import java.util.List;

public record LinkForUpdateCheck(Long id, String url, List<Long> tgChatIds, Instant lastUpdatedAt) {}
