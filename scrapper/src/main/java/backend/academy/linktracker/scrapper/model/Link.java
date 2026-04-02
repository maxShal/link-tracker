package backend.academy.linktracker.scrapper.model;

import java.time.Instant;
import java.util.List;

public record Link(Long id, String url, List<String> tags, Instant lastUpdatedAt) {}
