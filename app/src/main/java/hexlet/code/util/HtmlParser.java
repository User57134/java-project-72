package hexlet.code.util;

import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;

public class HtmlParser {
    public static Map<String, String> parse(String body) {
        Map<String, String> result = new HashMap<>();
        String title = null;
        String h1 = null;
        String description = null;

        var doc = Jsoup.parse(body);

        title = doc.title();

        var h1Tag = doc.selectFirst("h1");
        if (h1Tag != null) {
            h1 = h1Tag.text();
        }

        var metaDescription = doc.selectFirst("meta[name=description]");
        if (metaDescription != null) {
            description = metaDescription.attr("content");
        }

        result.put("title", title);
        result.put("h1", h1);
        result.put("description", description);

        return result;
    }
}
