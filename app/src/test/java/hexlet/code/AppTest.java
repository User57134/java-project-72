package hexlet.code;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.CheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.CorrectDisplay;
import hexlet.code.util.HtmlParser;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.TestConfig;
import java.io.IOException;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class AppTest {

    private Javalin app;

    private static HttpClient redirectableHttpClient = null;

    private static String readFixture(String fileName) throws IOException {
        Path fixturesPath = Path.of("src/test/resources/fixtures");
        Path filePath = fixturesPath.resolve(fileName);

        return Files.readString(filePath).trim();
    }

    private static HttpClient getRedirectableHttpClient() {
        /*
         *  Необходимо включить менеджер куки (чтобы не терялись сессионные атрибуты:
         *  в данном случае без менеджера потеряется флеш-сообщение
         *  об успешном или неудачном добавлении страницы),
         * */
        var cookieManager = new CookieManager();

        if (redirectableHttpClient == null) {
            redirectableHttpClient =
                    HttpClient.newBuilder()
                            .followRedirects(
                                    HttpClient.Redirect.NORMAL) // разрешить перенаправления
                            .cookieHandler(cookieManager) // добавить менеджер куки
                            .connectTimeout(
                                    Duration.ofSeconds(1)) // ограничить время ожидания 2 сек
                            .build();
        }

        return redirectableHttpClient;
    }

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
                    assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
                    assertThat(response.body().string().contains("Анализатор страниц")).isTrue();
                });
    }

    @Test
    public void testCheckForValidUrl() throws IOException {
        try (var mws = new MockWebServer()) {
            var successHtml = readFixture("success.html");

            // Установили содержимое ответаsuccess.html
            mws.enqueue(
                    new MockResponse()
                            .setResponseCode(HttpStatus.OK.getCode())
                            .setBody(successHtml));

            // Запустили сервер
            mws.start();

            // Подготовка тестового url
            Url testUrl = new Url(mws.url("/").toString());

            // Сохранение в базу
            UrlRepository.save(testUrl);

            // Убеждаемся, что проверок для testUrl еще не было
            assertThat(CheckRepository.getAllChecksForUrl(testUrl.getId()).size()).isEqualTo(0);

            var config = new TestConfig(false, true, getRedirectableHttpClient());

            JavalinTest.test(
                    app,
                    config,
                    (server, client) -> {
                        // Отправка обработчику Javalin запроса с адресом testUrl для проверки и
                        // сохранение результатов в базу
                        var response = client.post(NamedRoutes.urlCheckPath(testUrl.getId()));
                        assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
                        assertThat(response.body().string().contains("Страница успешно проверена"))
                                .isTrue();

                        // Убеждаемся, что для testUrl теперь есть проверка
                        var check = CheckRepository.getLatestChecksByUrl().get(testUrl.getId());
                        assertThat(check).isNotNull();

                        // Сверяем данные результатов проверки с successHtml
                        var tagValues = HtmlParser.parse(successHtml);

                        assertThat(check.getUrlId().equals(testUrl.getId())).isTrue();
                        assertThat(check.getTitle().equals(tagValues.get("title"))).isTrue();
                        assertThat(check.getH1().equals(tagValues.get("h1"))).isTrue();
                        assertThat(check.getDescription().equals(tagValues.get("description")))
                                .isTrue();
                    });
        }
    }

    @Test
    public void testCheckForNotFoundUrl() throws IOException {
        try (var mws = new MockWebServer()) {
            var testHtml = readFixture("not_found.html");

            // Установили содержимое ответа
            mws.enqueue(
                    new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.getCode())
                            .setBody(testHtml));

            // Запустили сервер
            mws.start();

            // Подготовка тестового url
            Url testUrl = new Url(mws.url("/").toString());

            // Сохранение в базу
            UrlRepository.save(testUrl);

            // Убеждаемся, что проверок для testUrl еще не было
            assertThat(CheckRepository.getAllChecksForUrl(testUrl.getId()).size()).isEqualTo(0);

            var config = new TestConfig(false, true, getRedirectableHttpClient());

            JavalinTest.test(
                    app,
                    config,
                    (server, client) -> {
                        // Отправка обработчику Javalin запроса с адресом testUrl для проверки и
                        // сохранение результатов в базу
                        var response = client.post(NamedRoutes.urlCheckPath(testUrl.getId()));
                        var body = response.getBody().string();
                        assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
                        assertThat(body.contains("Произошла ошибка при проверке")).isTrue();
                    });
        }
    }

    @Test
    public void testChekForInvalidUrl() throws IOException {
        /*
         * Домены .localhost и .invalid будут обрабатываться локально, поэтому
         * при попытке соединения по данному url мгновенно возникнет исключение
         * UnknownHostException.
         * */
        String invalidUrl = "http://dummy.invalid";
        // Подготовка тестового url
        Url testUrl = new Url(invalidUrl);

        // Сохранение в базу
        UrlRepository.save(testUrl);

        var config = new TestConfig(false, true, getRedirectableHttpClient());

        JavalinTest.test(
                app,
                config,
                (server, client) -> {
                    // Отправка обработчику Javalin запроса с адресом testUrl для проверки и
                    // сохранение результатов в базу
                    var response = client.post(NamedRoutes.urlCheckPath(testUrl.getId()));
                    assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());

                    var body = response.getBody().string();
                    assertThat(body.contains("Произошла ошибка при проверке")).isTrue();
                });
    }

    @Test
    public void testCheckRepository() throws IOException {
        Url testUrl1 = new Url("http://test1.com");
        Long id = UrlRepository.save(testUrl1);
        assertThat(id > 0).isTrue();

        Url testUrl2 = new Url("http://test2.com");
        id = UrlRepository.save(testUrl2);
        assertThat(id > 0).isTrue();

        Instant createdAt = Instant.now();

        UrlCheck urlCheck1 = new UrlCheck(testUrl1, 200);
        urlCheck1.setTitle("Url1 check1");
        urlCheck1.setCreatedAt(createdAt);
        Long id1 = CheckRepository.save(urlCheck1);
        assertThat(id1 > 0).isTrue();

        UrlCheck urlCheck2 = new UrlCheck(testUrl1, 200);
        urlCheck2.setTitle("Url1 check2");
        urlCheck2.setCreatedAt(createdAt.plus(1, ChronoUnit.HOURS));
        Long id2 = CheckRepository.save(urlCheck2);
        assertThat(id2 > 0).isTrue();
        assertThat(id2.equals(id1)).isFalse();

        UrlCheck urlCheck3 = new UrlCheck(testUrl1, 200);
        urlCheck3.setTitle("Url1 check3");
        urlCheck3.setCreatedAt(createdAt.plus(2, ChronoUnit.HOURS));
        Long id3 = CheckRepository.save(urlCheck3);
        assertThat(id3 > 0).isTrue();
        assertThat(id3.equals(id1)).isFalse();
        assertThat(id3.equals(id2)).isFalse();

        UrlCheck urlCheck4 = new UrlCheck(testUrl2, 200);
        urlCheck4.setTitle("Url2 check1");
        urlCheck4.setCreatedAt(createdAt);
        id = CheckRepository.save(urlCheck4);
        assertThat(id > 0).isTrue();

        var config = new TestConfig(false, true, getRedirectableHttpClient());

        JavalinTest.test(
                app,
                config,
                (server, client) -> {

                    // Проверить, что всего записей 2
                    var allChecks = CheckRepository.getLatestChecksByUrl();
                    assertThat(allChecks.size()).isEqualTo(2);

                    // Проверить, что для url#1 проверок 3
                    List<UrlCheck> checks = CheckRepository.getAllChecksForUrl(testUrl1.getId());
                    assertThat(checks.size()).isEqualTo(3);

                    // Проверить правильность времени последней проверки для url#1
                    var result = allChecks.get(testUrl1.getId());
                    assertThat(result != null).isTrue();

                    var lastCheckTime = checks.getFirst().getCreatedAt();
                    for (var check : checks) {
                        if (check.getCreatedAt().isAfter(lastCheckTime)) {
                            lastCheckTime = check.getCreatedAt();
                        }
                    }

                    assertThat(result.getCreatedAt().equals(lastCheckTime)).isTrue();
                });
    }

    /*
     *  Так как при добавлении url происходит перенаправление на другую страницу:
     *  после добавления url и происходит перенаправление на информационную страницу
     *  для этого url. Для обработки этого в конфигурации необходимо указать клиента
     *  способного автоматически переходить на другую страницу, либо обрабатывать такие
     *  переходы вручную.
     * */
    @Test
    public void testAddingUrl() {
        var config = new TestConfig(false, true, getRedirectableHttpClient());

        JavalinTest.test(
                app,
                config,
                (server, client) -> {
                    // Добавляем базовый url: должно добавиться
                    String baseUrl = "https://example.com";

                    String requestBody = "url=" + baseUrl;

                    var response = client.post(NamedRoutes.urlsPath(), requestBody);
                    assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());

                    String body = response.body().string();
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains("Страница успешно добавлена")).isTrue();
                    assertThat(body.contains(baseUrl)).isTrue();

                    // Пробуем добавить вложенный url на основе базового: добавиться не должно
                    String extendexUrl = "https://example.com/some/path?p1=val1&p2=val2";
                    requestBody = "url=" + extendexUrl;
                    response = client.post(NamedRoutes.urlsPath(), requestBody);

                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains("Страница уже существует")).isTrue();
                    assertThat(body.contains(extendexUrl)).isFalse();
                    assertThat(body.contains(baseUrl)).isTrue();

                    // Пробуем добавить тотже базовый url c портом 8080: должно добавиться
                    String baseUrlWithPort = "https://example.com:8080";
                    requestBody = "url=" + baseUrlWithPort;

                    response = client.post(NamedRoutes.urlsPath(), requestBody);

                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains("Страница успешно добавлена")).isTrue();
                    assertThat(body.contains(baseUrlWithPort)).isTrue();

                    // Пробуем добавить тотже вложенный url c портом 8080: добавиться не должно
                    extendexUrl = "https://example.com:8080/some/path?p1=val1&p2=val2";
                    requestBody = "url=" + extendexUrl;
                    response = client.post(NamedRoutes.urlsPath(), requestBody);
                    body = response.body().string();
                    assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
                    assertThat(body.contains("Сайт")).isTrue();
                    assertThat(body.contains("Страница уже существует")).isTrue();
                    assertThat(body.contains(extendexUrl)).isFalse();

                    assertThat(response.body().string().contains(baseUrl)).isTrue();

                    // Пробуем добавить некорректный url: добавиться не должно
                    extendexUrl = "kfjslkdj203jljfkd920232";
                    requestBody = "url=" + extendexUrl;
                    response = client.post(NamedRoutes.urlsPath(), requestBody);
                    body = response.body().string();
                    assertThat(response.code())
                            .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.getCode());
                    assertThat(response.body().string().contains("Анализатор страниц")).isTrue();
                    assertThat(body.contains("Некорректный URL")).isTrue();
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

                    /* Стандартный HttpClient в Java при редиректах NORMAL
                     * для некоторых статусов (cогласно спецификации HTTP)
                     * может сохранять исходный метод запроса: то есть после
                     * запроса 'DELETE /urls/id' будет выполняться запрос
                     * DELETE /urls, а не GET /urls.
                     * Поэтому автоматический редирект не подходит и нужно
                     * вручную после вызова метода DELETE получить
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

    @Test
    public void urlRepositoryTest() throws SQLException {
        Url u1 = new Url("https://site1.io");
        Url u2 = new Url("https://site2.com");
        Url u3 = new Url("https://site3.ru");

        UrlRepository.save(u1);
        UrlRepository.save(u2);
        UrlRepository.save(u3);

        var urls = UrlRepository.getEntities();
        assertThat(urls.size()).isEqualTo(3);

        for (var url : urls) {
            // Проверка поиска по идентификатору
            assertThat(UrlRepository.find(url.getId()).isPresent()).isTrue();

            // Проверка поиска по описанию
            assertThat(UrlRepository.search(url.getName())).isNotEqualTo(0L);

            // Удаление
            assertThat(UrlRepository.delete(url.getId())).isTrue();

            // Проверка поиска по описанию
            assertThat(UrlRepository.find(url.getId()).isPresent()).isFalse();

            // Проверка поиска по идентификатору
            assertThat(UrlRepository.search(url.getName())).isEqualTo(Optional.empty());
        }

        for (var url : urls) {
            UrlRepository.save(url);
        }

        UrlRepository.deleteAll();

        urls = UrlRepository.getEntities();
        assertThat(urls.size()).isEqualTo(0);
    }

    @Test
    public void urlCheckAddingTest() throws SQLException {
        Url u1 = new Url("https://site1.io");
        Url u2 = new Url("https://site2.com");
        Url u3 = new Url("https://site3.ru");

        UrlRepository.save(u1);
        UrlRepository.save(u2);
        UrlRepository.save(u3);

        var urls = UrlRepository.getEntities();
        assertThat(urls.size()).isEqualTo(3);

        for (var url : urls) {
            // Проверка поиска по идентификатору
            assertThat(UrlRepository.find(url.getId()).isPresent()).isTrue();

            // Проверка поиска по описанию
            assertThat(UrlRepository.search(url.getName())).isNotEqualTo(0L);

            // Удаление
            assertThat(UrlRepository.delete(url.getId())).isTrue();

            // Проверка поиска по описанию
            assertThat(UrlRepository.find(url.getId()).isPresent()).isFalse();

            // Проверка поиска по идентификатору
            assertThat(UrlRepository.search(url.getName())).isEqualTo(Optional.empty());
        }

        for (var url : urls) {
            UrlRepository.save(url);
        }

        UrlRepository.deleteAll();

        urls = UrlRepository.getEntities();
        assertThat(urls.size()).isEqualTo(0);
    }

    @Test
    public void testCorretDisplay() {
        int limit = 10;
        String testString = "0123456789abcdef";
        String expectedString = "0123456...";

        assertThat(CorrectDisplay.apply(testString)).isEqualTo(testString);
        assertThat(CorrectDisplay.apply(null)).isEqualTo("");
        assertThat(CorrectDisplay.apply(testString, 10)).isEqualTo(expectedString);
    }
}
