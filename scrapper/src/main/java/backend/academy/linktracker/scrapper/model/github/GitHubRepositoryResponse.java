package backend.academy.linktracker.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record GitHubRepositoryResponse(
        //@JsonProperty("pushed_at") String pushedAt
        GitHubUserResponse user,
        @JsonProperty("created_at") String createdAt,
/*        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("closed_at") String closedAt,*/
        String title,
        String body,
        @JsonProperty("pull_request")
        GitHubPullRequestResponse pullRequest
) {}
/*user.login
*"created_at"
 "updated_at"
*"closed_at"
*"title"
*"body"
* */
