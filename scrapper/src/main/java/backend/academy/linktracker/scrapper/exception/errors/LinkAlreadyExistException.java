package backend.academy.linktracker.scrapper.exception.errors;

public class LinkAlreadyExistException extends RuntimeException {
    public LinkAlreadyExistException(String message) {
        super(message);
    }
}
