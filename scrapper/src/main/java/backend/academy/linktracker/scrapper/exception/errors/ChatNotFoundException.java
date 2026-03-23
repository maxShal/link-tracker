package backend.academy.linktracker.scrapper.exception.errors;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(String message) {
        super(message);
    }
}
