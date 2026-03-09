package backend.academy.linktracker.bot.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class UserStateStorage {

    private final Map<Long, UserState> states = new ConcurrentHashMap<>();
    private final Map<Long, String> pendingLinks = new ConcurrentHashMap<>();

    public UserState getState(long chatId) {
        return states.getOrDefault(chatId, UserState.IDLE);
    }

    public void setState(long chatId, UserState state) {
        states.put(chatId, state);
    }

    public void clearState(long chatId) {
        states.remove(chatId);
        pendingLinks.remove(chatId);
    }

    public void setPendingLink(long chatId, String link) {
        pendingLinks.put(chatId, link);
    }

    public String getPendingLink(long chatId) {
        return pendingLinks.get(chatId);
    }
}
