package hexlet.code.util;

public class NamedRoutes {

    public static String urlsPath() {
        return "/urls";
    }

    public static String urlPath(Long id) {
        return urlPath(String.valueOf(id));
    }

    public static String urlPath(String id) {
        return "/urls/" + id;
    }

    public static String root() {
        return "/";
    }

    public static String urlCheckPath(Long id) {
        return urlPath(String.valueOf(id));
    }

    public static String urlCheckPath(String id) {
        return "/urls/" + id + "/checks";
    }
}
