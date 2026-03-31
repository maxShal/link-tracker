package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.model.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.model.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.model.response.LinkResponse;
import backend.academy.linktracker.scrapper.model.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.service.LinksService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/links")
public class LinksController {

    private final LinksService linksService;

    @GetMapping
    public ResponseEntity<@NotNull ListLinksResponse> getAllLinks(
            @RequestHeader("Tg-Chat-Id") Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        ListLinksResponse linksResponse = linksService.getAllLinks(chatId, page, size);
        return ResponseEntity.ok(linksResponse);
    }

    @PostMapping
    public ResponseEntity<@NotNull LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody AddLinkRequest addLinkRequest) {
        LinkResponse linkResponse = linksService.addLink(chatId, addLinkRequest);
        return ResponseEntity.ok(linkResponse);
    }

    @DeleteMapping
    public ResponseEntity<@NotNull LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody RemoveLinkRequest removeLinkRequest) {
        LinkResponse linkResponse = linksService.deleteLink(chatId, removeLinkRequest);
        return ResponseEntity.ok(linkResponse);
    }
}
