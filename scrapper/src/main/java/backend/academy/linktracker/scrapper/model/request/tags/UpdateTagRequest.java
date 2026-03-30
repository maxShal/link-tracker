package backend.academy.linktracker.scrapper.model.request.tags;

public record UpdateTagRequest(String url, String oldTag, String newTag) {}
