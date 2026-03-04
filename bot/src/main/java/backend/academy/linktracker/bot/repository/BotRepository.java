package backend.academy.linktracker.bot.repository;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BotRepository implements IBotRepository
{
    private final Set<Long> chats =  ConcurrentHashMap.newKeySet();

    @Override
    public boolean isOld(Long id) {
        return chats.contains(id);
    }

    @Override
    public void save(Long id) {
        chats.add(id);
    }
}
