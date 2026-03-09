package backend.academy.linktracker.scrapper.exception;

import backend.academy.linktracker.scrapper.exception.errors.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.errors.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.errors.LinkAlreadyExistException;
import backend.academy.linktracker.scrapper.exception.errors.LinkNotFoundException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> requestException(MethodArgumentNotValidException exception) {
        var body = new ApiErrorResponse(
                "Некорректные параметры запроса",
                "400",
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                List.of());
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> chatAlreadyExistException(ChatAlreadyExistsException exception) {
        var body = new ApiErrorResponse(
                "Чат уже существует", "409", exception.getClass().getSimpleName(), exception.getMessage(), List.of());
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<@NotNull ApiErrorResponse> badRequest(Exception exception) {
        var body = new ApiErrorResponse(
                "Некорректный запрос", "400", exception.getClass().getSimpleName(), exception.getMessage(), List.of());
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> chatNotFoundException(ChatNotFoundException exception) {
        var body = new ApiErrorResponse(
                "Чат не существует", "404", exception.getClass().getSimpleName(), exception.getMessage(), List.of());
        return ResponseEntity.status(404).body(body);
    }

    @ExceptionHandler(LinkAlreadyExistException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> linkAlreadyExistException(LinkAlreadyExistException exception) {
        var body = new ApiErrorResponse(
                "Ссылка уже отслеживается",
                "409",
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                List.of());
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> linkExistOrNotFoundException(LinkNotFoundException exception) {
        var body = new ApiErrorResponse(
                "Cсылка не найдена", "404", exception.getClass().getSimpleName(), exception.getMessage(), List.of());
        return ResponseEntity.status(404).body(body);
    }
}
