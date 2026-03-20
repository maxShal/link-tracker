package backend.academy.linktracker.bot.handler;

public interface ITrackHandler {
    public boolean supports(long chatId);

    public boolean handle(long chatId, String text);
}
