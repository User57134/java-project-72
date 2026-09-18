package hexlet.code.dto.urls;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class UrlPage {
    private Url url;

    @Setter private List<UrlCheck> checks;

    public UrlPage(Url aUrl) {
        url = aUrl;
    }
}
