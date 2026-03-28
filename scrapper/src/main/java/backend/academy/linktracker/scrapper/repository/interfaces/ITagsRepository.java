package backend.academy.linktracker.scrapper.repository.interfaces;

import java.util.List;

public interface ITagsRepository {
    void saveTag(Long linkId, List<String> tags);

    boolean existsTag(Long linkId, String tag);

    List<String> findAllTagsByLinkId(Long linkId);
}
