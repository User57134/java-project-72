package hexlet.code.controller;

import hexlet.code.dto.Flash;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.CheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.Formatter;
import hexlet.code.util.HtmlParser;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.UrlValidationException;
import io.javalin.http.*;
import java.sql.SQLException;
import java.util.*;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UrlsController {

    // Обработчик запроса на отображение главной страницы (формы добавления сайта)
    public static void build(Context ctx) {
        ctx.render("index.jte");
    }

    // Обработчик запроса на отображение сводной страницы со списком сайтов
    public static void showAll(Context ctx) throws SQLException {
        var urls = UrlRepository.getEntities();

        var checks = CheckRepository.getLatestChecksByUrl();

        var page = new UrlsPage(urls, checks);
        ctx.render("urls/index.jte", Map.of("page", page));
    }

    // Обработчик запроса на отображение страницы для сайта
    public static void show(Context ctx) throws SQLException {
        Long id =
                ctx.pathParamAsClass("id", Long.class)
                        .required()
                        .check(idValue -> idValue > 0, "id должен быть больше 0")
                        .getOrThrow(
                                errors ->
                                        new BadRequestResponse(
                                                "Ошибка идентификатора: " + errors.toString()));

        var url =
                UrlRepository.find(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundResponse(
                                                "Entity with id = " + id + " not found"));

        var page = new UrlPage(url);

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
    }

    // Обработчик запроса на добавление сайта
    public static void create(Context ctx) throws SQLException {
        String site = ctx.formParam("url");

        var url = Formatter.getBaseUrl(site).orElseThrow(() -> new UrlValidationException(site));

        var result = UrlRepository.search(url.toString());

        long id = 0;
        if (result.isEmpty()) {
            id = UrlRepository.save(new Url(url.toString()));

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("status", Flash.SUCCESS);
        } else {
            id = result.get().getId();

            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("status", Flash.FAIL);
        }

        ctx.redirect(NamedRoutes.urlPath(id));
    }

    // Обработчик запроса на добавление сайта
    public static void createCheck(Context ctx) throws SQLException {
        Long id =
                ctx.pathParamAsClass("id", Long.class)
                        .required()
                        .check(idValue -> idValue > 0, "id должен быть больше 0")
                        .getOrThrow(
                                errors ->
                                        new BadRequestResponse(
                                                "Ошибка идентификатора: " + errors.toString()));

        var url =
                UrlRepository.find(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundResponse(
                                                "Entity with id = " + id + " not found"));

        check(url)
                .ifPresentOrElse(
                        uc -> {
                            if (CheckRepository.save(uc) == 0L) {
                                throw new InternalServerErrorResponse(
                                        "Ошибка при сохранении данных о проверке сайта.");
                            }

                            ctx.sessionAttribute("flash", "Страница успешно проверена");
                            ctx.sessionAttribute("status", Flash.SUCCESS);
                        },
                        () -> {
                            ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
                            ctx.sessionAttribute("status", Flash.FAIL);
                        });

        ctx.redirect(NamedRoutes.urlPath(id));
    }

    // Обработчик запроса на удаление сайта
    public static void delete(Context ctx) throws SQLException {
        Long id =
                ctx.pathParamAsClass("id", Long.class)
                        .required()
                        .check(idValue -> idValue > 0, "id должен быть больше 0")
                        .getOrThrow(
                                errors ->
                                        new BadRequestResponse(
                                                "Ошибка идентификатора: " + errors.toString()));

        if (UrlRepository.delete(id)) {
            ctx.redirect(NamedRoutes.urlsPath());
        } else {
            throw new InternalServerErrorResponse("Ошибка при удалении сайта: " + id);
        }
    }

    private static Optional<UrlCheck> check(Url url) {
        try {
            var response = Unirest.get(url.getName()).asString();

            if (response.getStatus() < HttpStatus.BAD_REQUEST.getCode()) {
                var body = response.getBody();

                var tagValues = HtmlParser.parse(body);

                return Optional.of(
                        new UrlCheck(
                                url,
                                response.getStatus(),
                                tagValues.get("title"),
                                tagValues.get("h1"),
                                tagValues.get("description")));
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return Optional.empty();
    }
}
