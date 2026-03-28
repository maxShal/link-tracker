package backend.academy.linktracker.bot.model.commands;

public interface CommandHandler {
    boolean supports(String cmd);

    void handler(long chatId, String command);
}
