package backend.academy.linktracker.scrapper.repository.interfaces;

import backend.academy.linktracker.scrapper.model.Tag;
import java.util.List;

public interface ITagsRepository {
    void saveTag(Long linkId, List<String> tags);

    boolean existsTag(Long linkId, String tag);

    List<String> findAllTagsByLinkId(Long linkId);

    Tag deleteTag(Long chatId, String url, String tag);
}
