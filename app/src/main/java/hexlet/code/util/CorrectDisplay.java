package hexlet.code.util;

public class CorrectDisplay {
    private static final int DEFAULT_CHARACTERS_LIMIT = 200;

    public static String apply(String text, int charactersLimit) {
        if (text != null) {
            if (text.length() <= charactersLimit) {
                return text;
            } else {
                String suffix = "...";
                return text.substring(0, charactersLimit - suffix.length()) + suffix;
            }
        } else {
            return "";
        }
    }

    public static String apply(String text) {
        return apply(text, DEFAULT_CHARACTERS_LIMIT);
    }
}
