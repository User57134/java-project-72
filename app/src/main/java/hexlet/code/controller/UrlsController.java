package hexlet.code.controller;

import hexlet.code.dto.Flash;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.CheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.ErrorReport;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.net.URI;
import java.net.URL;
import java.util.*;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;

@Slf4j
public class UrlsController {

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

    // Обработчик запроса на отображение главной страницы (формы добавления сайта)
    public static void build(Context ctx) {
        ctx.render("index.jte");
    }

    // Обработчик запроса на отображение сводной страницы со списком сайтов
    public static void showAll(Context ctx) {
        var urls = UrlRepository.getEntities();

        var checks = CheckRepository.getLatestChecksByUrl();

        var page = new UrlsPage(urls, checks);
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

                Flash flash = new Flash();
                String flashMessage = ctx.consumeSessionAttribute("flash");
                if (flashMessage != null) {
                    Integer status = ctx.consumeSessionAttribute("status");

                    if (status != null) {
                        flash.setFlash(flashMessage, status);
                    }
                }

                var checks = CheckRepository.getAllChecksForUrl(id);
                page.setChecks(checks);

                ctx.render("urls/show.jte", Map.of("page", page, "flash", flash));
                return;
            }
        }

        ErrorReport.send(ctx, HttpStatus.NOT_FOUND, "Некорректный идентификатор сайта: " + sid);
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
                ctx.sessionAttribute("status", 1);
            } else {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("status", -1);
            }

            ctx.redirect(NamedRoutes.urlPath(id));

        } else {
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);

            ctx.render(
                    "index.jte", Map.of("input", site, "flash", new Flash("Некорректный URL", -1)));
        }
    }

    public static Map<String, String> parseHtml(String body) {
        Map<String, String> result = new HashMap<>();
        String title = null;
        String h1 = null;
        String description = null;

        var doc = Jsoup.parse(body);

        title = doc.title();

        var h1Tag = doc.selectFirst("h1");
        if (h1Tag != null) {
            h1 = h1Tag.text();
        }

        var metaDescription = doc.selectFirst("meta[name=description]");
        if (metaDescription != null) {
            description = metaDescription.attr("content");
        }

        result.put("title", title);
        result.put("h1", h1);
        result.put("description", description);

        return result;
    }

    private static UrlCheck check(Url url) {
        try {
            var response = Unirest.get(url.getName()).asString();

            if (response.getStatus() >= HttpStatus.BAD_REQUEST.getCode()) {
                return null;
            }

            var body = response.getBody();

            var tagValues = parseHtml(body);

            return new UrlCheck(
                    url,
                    response.getStatus(),
                    tagValues.get("title"),
                    tagValues.get("h1"),
                    tagValues.get("description"));
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return null;
    }

    // Обработчик запроса на добавление сайта
    public static void createCheck(Context ctx) {
        var sid = ctx.pathParam("id");
        long id = NumberUtils.toLong(sid, 0L);

        if (id != 0) {
            var url = UrlRepository.find(id);

            if (url.isPresent()) {
                var ulrCheck = check(url.get());

                if (ulrCheck != null) {
                    if (CheckRepository.save(ulrCheck) == 0L) {
                        ErrorReport.send(
                                ctx,
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Ошибка при сохранении проверки");
                        return;
                    }

                    ctx.sessionAttribute("flash", "Страница успешно проверена");
                    ctx.sessionAttribute("status", 1);

                } else {
                    log.info("UrsController::createCheck(null)");

                    ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
                    ctx.sessionAttribute("status", -1);
                }

                ctx.redirect(NamedRoutes.urlPath(id));

                return;
            }
        }

        ErrorReport.send(ctx, HttpStatus.NOT_FOUND, "Некорректный идентификатор сайта: " + sid);
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
