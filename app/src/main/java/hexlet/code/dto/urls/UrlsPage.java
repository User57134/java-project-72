package hexlet.code.dto.urls;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UrlsPage {
    private List<Url> urls;
    private Map<Long, UrlCheck> checks = new HashMap<>();

    public UrlsPage(List<Url> urlList) {
        urls = urlList;

        for (var url : urls) {
            checks.put(url.getId(), null);
        }
    }

    public boolean setLastCheckForUrl(long urlId, UrlCheck check) {
        if (checks.containsKey(urlId)) {
            checks.put(urlId, check);
            return true;
        } else {
            return false;
        }
    }

    public UrlCheck getLastCheckForUrl(long urlId) {
        return checks.getOrDefault(urlId, null);
    }
}
