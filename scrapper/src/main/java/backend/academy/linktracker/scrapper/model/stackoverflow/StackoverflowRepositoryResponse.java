package backend.academy.linktracker.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StackoverflowRepositoryResponse {

    /*
        public record StackoverflowQuestionResponse(List<QuestionItem> items) {
            public record QuestionItem(
                    @JsonProperty("last_edit_date") Long lastActivityDate) {}
        }
    */

    public record StackoverflowQuestionResponse(List<QuestionItem> items) {}

    public record QuestionItem(@JsonProperty("question_id") Long questionId, String title) {}

    public record AnswersResponse(List<AnswerItem> items) {}

    public record AnswerItem(
            @JsonProperty("answer_id") Long answerId,
            @JsonProperty("creation_date") Long creationDate,
            String body,
            Owner owner) {}

    public record CommentsResponse(List<CommentItem> items) {}

    public record CommentItem(
            @JsonProperty("comment_id") Long commentId,
            @JsonProperty("creation_date") Long creationDate,
            String body,
            Owner owner) {}

    public record Owner(@JsonProperty("display_name") String displayName) {}
}
