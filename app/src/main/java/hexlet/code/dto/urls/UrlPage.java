package hexlet.code.dto.urls;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.util.Flash;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class UrlPage {
    Url url;

    @Setter List<UrlCheck> checks;

    @Setter private Flash flash;

    public UrlPage(Url aUrl) {
        url = aUrl;
    }
}
