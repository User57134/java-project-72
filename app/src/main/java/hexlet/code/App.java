package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.stream.Collectors;

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

    private static String getDatabaseUrl() {
        // Получаем url базы данных из переменной окружения JDBC_DATABASE_URL
        // Если она не установлена, используем базу в памяти;
        // DB_CLOSE_DELAY = -1 - указание базе H2 закрываться при закрытии приложения,
        // по-умолчанию закрытие базы происходит при закрытии последнего активного соединения
        return System.getenv()
                .getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1");
    }

    private static String getCreateDbSqlScript() {
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
        var hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(getDatabaseUrl());

        var dataSource = new HikariDataSource(hikariConfig);
        BaseRepository.dataSource = dataSource;

        // Create database urls
        var createDbSql = getCreateDbSqlScript();
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
                            config.routes.get("/", ctx -> ctx.result("Hello World!"));
                        });

        return app;
    }
}
