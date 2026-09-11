package hexlet.code;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.TestConfig;
import java.io.IOException;
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
                    assertThat(response.body().string()).contains("Анализатор страниц");
                });
    }

    @Test
    public void testUrls() {
        var httpClient =
                HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

        var config = new TestConfig(true, true, httpClient);

        JavalinTest.test(
                app,
                config,
                (server, client) -> {
                    var response = client.post(NamedRoutes.urlsPath(), "url=https://example.com");
                    assertThat(response.code()).isEqualTo(200);
                });
    }
}
