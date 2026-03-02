package backend.academy.linktracker.scrapper.exception.errors;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String message) {
        super(message);
    }
}
