package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.errors.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.exception.errors.TagNotFoundException;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.model.request.tags.AddTagRequest;
import backend.academy.linktracker.scrapper.model.request.tags.RemoveTagRequest;
import backend.academy.linktracker.scrapper.model.request.tags.UpdateTagRequest;
import backend.academy.linktracker.scrapper.model.response.ListTagsResponse;
import backend.academy.linktracker.scrapper.model.response.TagResponse;
import backend.academy.linktracker.scrapper.repository.interfaces.ILinksRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITagsRepository;
import backend.academy.linktracker.scrapper.repository.interfaces.ITgChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagsService {
    private final ITgChatRepository chatRepository;
    private final ILinksRepository linksRepository;
    private final ITagsRepository tagsRepository;

    public void addTags(Long chatId, AddTagRequest addTagRequest) {
        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат" + chatId + " не найден");
        }
        if (!linksRepository.existsLink(chatId, addTagRequest.url())) {
            throw new LinkNotFoundException("Ссылка: " + addTagRequest.url() + "не найдена");
        }

        Long linkId = linksRepository.findLinkIdByUrl(addTagRequest.url());

        tagsRepository.saveTag(linkId, addTagRequest.tags());
    }

    public ListTagsResponse getTags(Long chatId, String url) {
        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат" + chatId + " не найден");
        }
        if (!linksRepository.existsLink(chatId, url)) {
            throw new LinkNotFoundException("Ссылка: " + url + "не найдена");
        }

        Long linkId = linksRepository.findLinkIdByUrl(url);

        List<String> tags = tagsRepository.findAllTagsByLinkId(linkId);
        return new ListTagsResponse(tags);
    }

    public TagResponse deleteTag(Long chatId, RemoveTagRequest removeTagRequest) {
        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат " + chatId + " не найден");
        }

        if (!linksRepository.existsLink(chatId, removeTagRequest.url())) {
            throw new LinkNotFoundException("Ссылка " + removeTagRequest.url() + " не найдена");
        }
        Tag removed = tagsRepository.deleteTag(chatId, removeTagRequest.url(), removeTagRequest.tag());
        return new TagResponse(removed.id(), removed.tag());
    }

    public void updateTag(Long chatId, UpdateTagRequest updateTagRequest) {
        if (!chatRepository.existsChats(chatId)) {
            throw new ChatNotFoundException("Чат " + chatId + " не найден");
        }

        Long linkId = linksRepository.findLinkIdByUrl(updateTagRequest.url());

        if (tagsRepository.existsTag(linkId, updateTagRequest.oldTag())) {
            throw new TagNotFoundException("Тег " + updateTagRequest.oldTag() + " не найден");
        }

        tagsRepository.deleteTag(chatId, updateTagRequest.url(), updateTagRequest.oldTag());
        tagsRepository.saveTag(linkId, List.of(updateTagRequest.newTag()));
    }
}
