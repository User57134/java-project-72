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

    //
    //    public static String urlEditPath(Long id) {
    //        return urlEditPath(String.valueOf(id));
    //    }
    //
    //
    //    public static String urlEditPath(String id) {
    //        return "/urls/" + id + "/edit";
    //    }
    //
    //
    //    public static String buildUrlPath() {
    //        return "/urls/build";
    //    }

}
