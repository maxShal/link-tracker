package backend.academy.linktracker.scrapper.exception.errors;

public class ChatAlreadyExistsException extends RuntimeException {
    public ChatAlreadyExistsException(String message) {
        super(message);
    }
}
