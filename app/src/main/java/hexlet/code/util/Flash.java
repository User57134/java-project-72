package hexlet.code.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Flash {
    @NonNull private String message;

    private boolean successed;
}
