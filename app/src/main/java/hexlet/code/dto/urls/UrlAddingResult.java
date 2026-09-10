package hexlet.code.dto.urls;

import lombok.*;

@AllArgsConstructor
public class UrlAddingResult {
    @Getter private final String input;

    @Getter private String flash;

    @NonNull private Boolean success;

    public boolean isSuccessed() {
        return success;
    }
}
