package backend.academy.linktracker.scrapper.repository.interfaces;

public interface ITgChatRepository {
    void saveChat(Long id);

    boolean existsChats(Long id);

    void deleteChat(Long id);
}
