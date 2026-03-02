package backend.academy.linktracker.scrapper.exception;

import backend.academy.linktracker.scrapper.exception.errors.ChatAlreadyExistException;
import backend.academy.linktracker.scrapper.exception.errors.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.errors.LinkAlreadyExistException;
import backend.academy.linktracker.scrapper.exception.errors.LinkNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> requestException(MethodArgumentNotValidException exception) {
        var body = new ApiErrorResponse(
            "Некорректные параметры запроса",
            "400",
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            List.of()
        );
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(ChatAlreadyExistException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> chatAlreadyExistException(ChatAlreadyExistException exception) {
        var body = new ApiErrorResponse(
            "Чат уже существует",
            "409",
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            List.of()
        );
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> chatNotFoundException(ChatNotFoundException exception) {
        var body = new ApiErrorResponse(
            "Чат не существует",
            "404",
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            List.of()
        );
        return ResponseEntity.status(404).body(body);
    }

    @ExceptionHandler(LinkAlreadyExistException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> linkAlreadyExistException(LinkAlreadyExistException exception) {
        var body = new ApiErrorResponse(
            "Ссылка уже отслеживается",
            "409",
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            List.of()
        );
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> linkExistOrNotFoundException(LinkNotFoundException exception) {
        var body = new ApiErrorResponse(
            "Cсылка не найдена",
            "404",
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            List.of()
        );
        return ResponseEntity.status(404).body(body);
    }
}
