package hexlet.code.dto.urls;

import hexlet.code.model.Url;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UrlsPage {
    List<Url> urls;
}
