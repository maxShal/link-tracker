package backend.academy.linktracker.bot.repository;

public interface IBotRepository {
    boolean isOld(Long id);

    void save(Long id);
}
