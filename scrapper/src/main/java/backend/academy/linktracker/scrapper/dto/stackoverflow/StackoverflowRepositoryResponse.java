package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StackoverflowRepositoryResponse {

    public record StackoverflowQuestionResponse(List<QuestionItem> items) {
        public record QuestionItem(
                @JsonProperty("last_activity_date") Long lastActivityDate) {}
    }
}
