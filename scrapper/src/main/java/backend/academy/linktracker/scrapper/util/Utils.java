package backend.academy.linktracker.scrapper.util;

public final class Utils {
    public static final String GITHUB = "^https://github\\.com/([^/]+)/([^/]+?)/?$";
    public static final String STACKOVERFLOW = "^https://(?:ru\\.)?stackoverflow\\.com/questions/(\\d+).*$";
    public static final String HTML_REGEX = "<[^>]*>";
    public static final int MAX_POST_LENGTH = 200;
}
