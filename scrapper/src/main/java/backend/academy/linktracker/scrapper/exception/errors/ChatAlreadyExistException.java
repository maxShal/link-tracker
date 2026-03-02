package backend.academy.linktracker.scrapper.exception.errors;

public class ChatAlreadyExistException extends RuntimeException
{
    public ChatAlreadyExistException(String message)
    {
        super(message);
    }
}
