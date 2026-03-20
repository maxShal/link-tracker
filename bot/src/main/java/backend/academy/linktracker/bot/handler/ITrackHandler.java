package backend.academy.linktracker.bot.handler;

public interface ITrackHandler {
    boolean supports(long chatId);

    boolean handle(long chatId, String text);
}
