package hexlet.code.util;

import hexlet.code.dto.Flash;
import lombok.Getter;

@Getter
public class UrlValidationException extends RuntimeException {
    private final Flash flash;
    private final String input;

    public UrlValidationException(String site) {
        super("Некорректный URL: " + site);
        flash = new Flash("Некорректный URL", Flash.FAIL);
        input = site;
    }
}
