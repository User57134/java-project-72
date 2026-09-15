package hexlet.code.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UrlCheck {
    private Long id;
    private int statusCode;
    private String title;
    private String h1;
    private String description;
    private Long urlId;
    private Instant createdAt;

    public UrlCheck(Url url, int status) {
        urlId = url.getId();
        statusCode = status;
        createdAt = Instant.now();
    }
}
