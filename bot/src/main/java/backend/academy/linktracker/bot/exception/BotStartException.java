package backend.academy.linktracker.bot.exception;

public class BotStartException extends RuntimeException {
    public BotStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
