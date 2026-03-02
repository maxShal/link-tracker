package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.commands.CommandHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StartCommand implements CommandHandler {

    private final MessageSender sender;
    @Override
    public boolean supports(String cmd) {
        return cmd.equals("start");
    }

    @Override
    public void handler(long chatId, String command) {
        sender.send(chatId, "command");
    }
}
