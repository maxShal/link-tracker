package backend.academy.linktracker.scrapper.model.request.tags;

import java.util.List;

public record AddTagRequest(String url, List<String> tags) {}
