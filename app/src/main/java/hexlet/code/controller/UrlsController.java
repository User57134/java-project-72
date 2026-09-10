package hexlet.code.controller;

import hexlet.code.App;
import hexlet.code.dto.urls.UrlAddingResult;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.ErrorReport;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlsController {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private static UrlAddingResult checkAddingResult(Context ctx) {
        String flash = ctx.consumeSessionAttribute("flash");

        UrlAddingResult result = null;

        // Если сообщения нет, значит страница вызывается впервые
        // Если сообщение есть, значит были ошибки и страница вызывается повторно
        if (flash != null) {
            Boolean status = ctx.consumeSessionAttribute("status");
            String input = ctx.consumeSessionAttribute("input");

            result = new UrlAddingResult(input, flash, status);
        }

        return result;
    }

    // Обработчик запроса на отображение главной страницы (формы добавления сайта)
    public static void build(Context ctx) {
        String flash = ctx.consumeSessionAttribute("flash");

        UrlAddingResult result = checkAddingResult(ctx);
        if (result != null) {
            ctx.render("index.jte", Map.of("result", result));
        } else {
            ctx.render("index.jte");
        }
    }

    // Обработчик запроса на отображение сводной страницы со списком сайтов
    public static void showAll(Context ctx) {
        var urls = UrlRepository.getEntities();
        var page = new UrlsPage(urls);
        ctx.render("urls/index.jte", Map.of("page", page));
    }

    // Обработчик запроса на отображение страницы для сайта
    public static void show(Context ctx) {
        var sid = ctx.pathParam("id");
        long id = NumberUtils.toLong(sid, 0L);

        if (id != 0) {
            var url = UrlRepository.find(id);

            if (url.isPresent()) {
                var page = new UrlPage(url.get());

                UrlAddingResult result = checkAddingResult(ctx);
                if (result != null) {
                    ctx.render("urls/show.jte", Map.of("page", page, "result", result));
                } else {
                    ctx.render("urls/show.jte", Map.of("page", page));
                }

                return;
            }
        }

        ErrorReport.send(ctx, HttpStatus.NOT_FOUND, "Некорректный идентификатор сайта: " + sid);
    }

    private static URL parseUrl(String site) {
        try {
            var url = new URI(site).toURL();
            var protocol = url.getProtocol();
            var host = url.getHost();
            var port = url.getPort();

            return URI.create(protocol + "://" + host + ((port != -1) ? (":" + port) : "")).toURL();
        } catch (Exception ex) {
            log.error("Некорректный формат URL: {}", site);
            return null;
        }
    }

    // Обработчик запроса на добавление сайта
    public static void create(Context ctx) {
        String site = ctx.formParam("url");

        URL url = parseUrl(site);
        if (url != null) {
            var id = UrlRepository.search(url.toString());

            if (id == 0L) {
                id = UrlRepository.save(new Url(url.toString()));

                if (id == 0L) {
                    ErrorReport.send(
                            ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при регистрации сайта");
                    return;
                }

                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("status", Boolean.TRUE);
                ctx.sessionAttribute("input", "");
            } else {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("status", Boolean.FALSE);
                ctx.sessionAttribute("input", "");
            }

            ctx.redirect(NamedRoutes.urlPath(id));

        } else {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("status", Boolean.FALSE);
            ctx.sessionAttribute("input", site);

            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);

            ctx.redirect(NamedRoutes.root());
        }
    }

    // Обработчик запроса на удаление сайта
    public static void delete(Context ctx) {
        var sid = ctx.pathParam("id");

        long id = NumberUtils.toLong(sid, 0L);
        if (id != 0) {
            var result = UrlRepository.delete(id);

            if (result) {
                ctx.redirect(NamedRoutes.urlsPath());
                return;
            }
        }

        ErrorReport.send(ctx, HttpStatus.NOT_FOUND, "Некорректный идентификатор сайта: " + sid);
    }
}
