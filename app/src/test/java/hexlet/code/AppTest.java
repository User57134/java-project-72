package hexlet.code;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import java.io.IOException;
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
}
