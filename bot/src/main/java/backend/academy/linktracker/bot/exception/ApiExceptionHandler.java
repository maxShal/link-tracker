package backend.academy.linktracker.bot.exception;

import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler()
    public ResponseEntity<ApiErrorResponse> handleException(Exception exception) {
        var body = new ApiErrorResponse(
            "",
            "400",
            exception.getClass().getSimpleName(),
            exception.getMessage(),
        );
        return  ResponseEntity.badRequest().body(body);
    }
}
