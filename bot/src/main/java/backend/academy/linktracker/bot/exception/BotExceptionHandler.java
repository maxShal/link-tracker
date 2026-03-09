package backend.academy.linktracker.bot.exception;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BotExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleBadJson(HttpMessageNotReadableException e) {
        var body = new ApiErrorResponse(
                "Некорректные параметры запроса", "400", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.badRequest().body(body);
    }
}
