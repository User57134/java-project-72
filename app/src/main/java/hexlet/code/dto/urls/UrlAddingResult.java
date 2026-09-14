package hexlet.code.dto.urls;

import hexlet.code.util.Flash;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UrlAddingResult {
    private String input;
    private Flash flash;
}
