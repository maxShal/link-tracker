package backend.academy.linktracker.scrapper.model.response;

import java.util.List;

public record LinkResponse(Long id, String url, List<String> tags) {}
