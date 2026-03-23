package backend.academy.linktracker.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepositoryResponse(
        @JsonProperty("pushed_at") String pushedAt) {}
