package hexlet.code;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin app = getApp();

        // адрес 0.0.0.0 позволяет пробрасывать порты и можно соединяться из
        // браузера в windows к приложению на сервере в WSL;
        // 127.0.0.1 и тп. не работают
        app.start("0.0.0.0", getPort());
    }

    private static int getPort() {
        // Получаем url базы данных из переменной окружения DATABASE_URL
        // Если она не установлена, используем базу в памяти
        var port = System.getenv().getOrDefault("PORT", "7070");

        return Integer.parseInt(port);
    }

    public static Javalin getApp() {
        var app =
                Javalin.create(
                        config -> {
                            config.bundledPlugins.enableDevLogging();
                            config.routes.get("/", ctx -> ctx.result("Hello World!"));
                        });

        return app;
    }
}
