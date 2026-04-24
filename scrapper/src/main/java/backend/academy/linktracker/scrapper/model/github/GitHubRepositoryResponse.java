package backend.academy.linktracker.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepositoryResponse(
        GitHubUserResponse user, @JsonProperty("created_at") String createdAt, String title, String body) {}
