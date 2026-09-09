package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.UrlsController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Path TEMPLATES_PATH = Path.of("src", "main", "resources", "templates");
    private static final Path STATIC_PATH = Path.of("src", "main", "resources", "static");
    private static final Path JTE_CLASSES_PATH = Path.of("jte-classes");

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "production");
    }

    private static int getPort() {
        // Получаем url базы данных из переменной окружения DATABASE_URL
        // Если она не установлена, используем базу в памяти
        var port = System.getenv().getOrDefault("PORT", "7070");

        return Integer.parseInt(port);
    }

    private static String getDatabaseUrl() {
        // Получаем url базы данных из переменной окружения JDBC_DATABASE_URL
        // Если она не установлена, используем базу в памяти;
        // DB_CLOSE_DELAY = -1 - указание базе H2 закрываться при закрытии приложения,
        // по-умолчанию закрытие базы происходит при закрытии последнего активного соединения
        return System.getenv()
                .getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1");
    }

    private static boolean isDevelopment() {
        return getMode().equals("development") && Files.isDirectory(TEMPLATES_PATH);
    }

    private static TemplateEngine createTemplateEngine() {
        if (isDevelopment()) {
            var codeResolver = new DirectoryCodeResolver(TEMPLATES_PATH);
            return TemplateEngine.create(codeResolver, JTE_CLASSES_PATH, ContentType.Html);
        }

        var classLoader = App.class.getClassLoader();
        var codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    private static String getCreationDbSqlScript() {
        try (var is = App.class.getClassLoader().getResourceAsStream("schema.sql")) {

            if (is != null) {
                var bufferedReader = new BufferedReader(new InputStreamReader(is));
                var sql = bufferedReader.lines().collect(Collectors.joining("\n"));
                return sql;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static Javalin getApp() {
        logger.info(
                "Applications is started in "
                        + (isDevelopment() ? " a development mode" : "production mode"));

        var hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(getDatabaseUrl());

        var dataSource = new HikariDataSource(hikariConfig);
        BaseRepository.dataSource = dataSource;

        // Create database urls
        var createDbSql = getCreationDbSqlScript();
        try (var connection = dataSource.getConnection()) {
            var statement = connection.createStatement();

            statement.execute(createDbSql);
        } catch (SQLException e) {
            throw new RuntimeException("Database interaction error: " + e.getMessage());
        }

        var app =
                Javalin.create(
                        config -> {
                            config.bundledPlugins.enableDevLogging();
                            if (isDevelopment()) {
                                config.staticFiles.add(STATIC_PATH.toString(), Location.EXTERNAL);
                            } else {
                                config.staticFiles.add("/static", Location.CLASSPATH);
                            }
                            config.fileRenderer(new JavalinJte(createTemplateEngine()));

                            config.routes.get(NamedRoutes.root(), UrlsController::build);
                        });

        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();

        // адрес 0.0.0.0 позволяет пробрасывать порты и можно соединяться из
        // браузера в windows к приложению на сервере в WSL;
        // 127.0.0.1 и тп. не работают
        app.start("0.0.0.0", getPort());
    }
}
