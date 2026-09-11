package hexlet.code;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.TestConfig;
import java.io.IOException;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AppTest {

    private Javalin app;

    @BeforeEach
    public final void setUp() throws IOException, SQLException {
        app = App.getApp();

        UrlRepository.deleteAll();
    }

    @Test
    public void testMainPage() {
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.get("/");
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(response.body().string().contains("Анализатор страниц")).isTrue();
                });
    }

    @Test
    public void testAddingUrl() {
        var cookieManager = new CookieManager();

        var httpClient =
                HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL) // разрешить перенаправления
                        .cookieHandler(cookieManager) // добавить менеджер куки
                        .build();

        /*
         *  Так как при добавлении url происходит перенаправление на другую страницу:
         *  добавляется url и перенаправляется на информационную страницу для этого
         *  url, то в конфигурации необходимо разрешить автоматическое перенаправление
         *  на другую страницу и включить менеджер куки (чтобы не терялись сессионные
         *  атрибуты: в данном случае без менеджера потеряется флеш-сообщение об успешном
         *  или неудачном добавлении страницы), либо обрабатывать переход вручную.
         * */
        var config = new TestConfig(false, true, httpClient);

        JavalinTest.test(
                app,
                config,
                (server, client) -> {
                    // Добавляем базовый url: должно добавиться
                    String baseUrl = "https://example.com";

                    String requestBody = "url=" + baseUrl;

                    var response = client.post(NamedRoutes.urlsPath(), requestBody);
                    assertThat(response.code()).isEqualTo(200);

                    String body = response.body().string();
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains("Страница успешно добавлена")).isTrue();
                    assertThat(body.contains(baseUrl)).isTrue();

                    // Пробуем добавить вложенный url на основе базового: добавиться не должно
                    String extendexUrl = "https://example.com/some/path?p1=val1&p2=val2";
                    requestBody = "url=" + extendexUrl;
                    response = client.post(NamedRoutes.urlsPath(), requestBody);

                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains("Страница уже существует")).isTrue();
                    assertThat(body.contains(extendexUrl)).isFalse();
                    assertThat(body.contains(baseUrl)).isTrue();

                    // Пробуем добавить тотже базовый url c портом 8080: должно добавиться
                    String baseUrlWithPort = "https://example.com:8080";
                    requestBody = "url=" + baseUrlWithPort;

                    response = client.post(NamedRoutes.urlsPath(), requestBody);

                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains("Страница успешно добавлена")).isTrue();
                    assertThat(body.contains(baseUrlWithPort)).isTrue();

                    // Пробуем добавить тотже вложенный url c портом 8080: добавиться не должно
                    extendexUrl = "https://example.com:8080/some/path?p1=val1&p2=val2";
                    requestBody = "url=" + extendexUrl;
                    response = client.post(NamedRoutes.urlsPath(), requestBody);
                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains("Страница уже существует")).isTrue();
                    assertThat(body.contains(extendexUrl)).isFalse();

                    assertThat(response.body().string().contains(baseUrl)).isTrue();
                });
    }

    @Test
    public void testUrlsPage() {
        Url u1 = new Url("https://site1.io");
        Url u2 = new Url("https://site2.com");
        Url u3 = new Url("https://site3.ru");
        Url u4 = new Url("https://site4.com");

        var id1 = UrlRepository.save(u1);
        var id2 = UrlRepository.save(u2);
        var id3 = UrlRepository.save(u3);

        JavalinTest.test(
                app,
                (server, client) -> {
                    // отображение всех сайтов
                    var response = client.get("/urls");
                    assertThat(response.code()).isEqualTo(200);

                    var body = response.body().string();
                    assertThat(body.contains(u1.getName())).isTrue();
                    assertThat(body.contains(u2.getName())).isTrue();
                    assertThat(body.contains(u3.getName())).isTrue();
                    assertThat(body.contains(u4.getName())).isFalse();

                    // Просмотр первого сайта
                    response = client.get("/urls/" + id1);
                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains(u1.getName())).isTrue();

                    // Просмотр второго сайта
                    response = client.get("/urls/" + id2);
                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains(u2.getName())).isTrue();

                    // Просмотр третьего сайта
                    response = client.get("/urls/" + id3);
                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains(u3.getName())).isTrue();

                    /* Стандартный HttpClient в Java при редиректах NORMAL для некоторых статусов (cогласно спецификации
                     * HTTP) может сохранять исходный метод запроса: то есть после запроса 'DELETE /urls/id' будет
                     * выполняться запрос DELETE /urls, а не GET /urls.
                     * Поэтому автоматический редирект не подходит и нужно вручную после вызова метода DELETE получить
                     * сообщение о перенаправлении и перейти по указанному адресу.
                     **/
                    // удаление первого сайта
                    response = client.delete("/urls/" + id1);

                    assertThat(response.code()).isEqualTo(302);
                    var redirect = response.headers().get("Location").getFirst();

                    response = client.get(redirect);
                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(body.contains(u1.getName())).isFalse();
                    assertThat(body.contains(u2.getName())).isTrue();
                    assertThat(body.contains(u3.getName())).isTrue();

                    // удаление второго сайта
                    response = client.delete("/urls/" + id2);

                    assertThat(response.code()).isEqualTo(302);
                    redirect = response.headers().get("Location").getFirst();

                    response = client.get(redirect);
                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(body.contains(u2.getName())).isFalse();
                    assertThat(body.contains(u3.getName())).isTrue();

                    // удаление третьего сайта
                    response = client.delete("/urls/" + id3);

                    assertThat(response.code()).isEqualTo(302);
                    redirect = response.headers().get("Location").getFirst();

                    response = client.get(redirect);
                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(body.contains(u3.getName())).isFalse();
                });
    }
}
