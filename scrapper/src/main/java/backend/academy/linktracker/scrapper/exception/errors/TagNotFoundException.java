package backend.academy.linktracker.scrapper.exception.errors;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(String message) {
        super(message);
    }
}
