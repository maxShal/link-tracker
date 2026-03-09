package backend.academy.linktracker.scrapper.response;

import java.util.List;

public record ListLinksResponse(List<LinkResponse> links, int size) {}
