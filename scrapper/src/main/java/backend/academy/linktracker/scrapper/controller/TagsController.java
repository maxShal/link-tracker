package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.model.request.tags.AddTagRequest;
import backend.academy.linktracker.scrapper.model.request.tags.RemoveTagRequest;
import backend.academy.linktracker.scrapper.model.request.tags.UpdateTagRequest;
import backend.academy.linktracker.scrapper.model.response.ListTagsResponse;
import backend.academy.linktracker.scrapper.service.TagsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagsController {
    private final TagsService tagsService;

    @GetMapping
    public ResponseEntity<@NotNull ListTagsResponse> getTags(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestParam String url) {
        ListTagsResponse listTagsResponse = tagsService.getTags(chatId, url);
        return ResponseEntity.ok(listTagsResponse);
    }

    @DeleteMapping
    public ResponseEntity<@NotNull Void> deleteTags(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody RemoveTagRequest removeTagRequest) {
        tagsService.deleteTag(chatId, removeTagRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<@NotNull Void> addTags(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody AddTagRequest addTagRequest) {
        tagsService.addTags(chatId, addTagRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<@NotNull Void> updateTag(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody UpdateTagRequest updateTagRequest) {
        tagsService.updateTag(chatId, updateTagRequest);
        return ResponseEntity.ok().build();
    }
}
