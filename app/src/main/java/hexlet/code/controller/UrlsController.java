package hexlet.code.controller;


import hexlet.code.App;
import hexlet.code.dto.urls.UrlAddingPage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.ErrorReport;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;


public class UrlsController {

    private static final Logger log = LoggerFactory.getLogger(App.class);


    // Обработчик запроса на отображение главной страницы (формы добавления сайта)
    public static void build(Context ctx) {
        String name = ctx.formParam("name");

        var page = new UrlAddingPage();

        ctx.render("index.jte", Map.of("page", page));
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
                ctx.render("urls/show.jte", Map.of("page", page));
                return;
            }
        }

        ErrorReport.send(ctx,HttpStatus.valueOf("NOT_FOUND"), "Некорректный идентификатор сайта: " + sid);
    }


    // Обработчик запроса на добавление сайта
    public static void create(Context ctx) {
        String name = ctx.formParam("name");

        URL site = null;
        try {
            var uri = new URI(name);
            site = uri.toURL();
        } catch (URISyntaxException ex) {
            log.error("UrlsController::create error: {}", ex.getMessage());
        } catch (MalformedURLException ex) {
            log.error("UrlsController::create error: {}", ex.getMessage());
        }

        if (site != null) {
            var id = UrlRepository.search(site.toString());

            if (id == 0L) {
               var url = new Url(site.toString());

               id = UrlRepository.save(url);

               if (id == 0L) {
                   ErrorReport.send(ctx, HttpStatus.valueOf("INTERNAL_SERVER_ERROR"), "Ошибка при регистрации сайта");
               }
            }

            ctx.redirect(NamedRoutes.urlPath(id));

        } else {
            var page = new UrlAddingPage(name, "Некорректный URL");

            ctx.status(HttpStatus.valueOf("VALIDATION_ERROR"));

            ctx.render(NamedRoutes.root(), Map.of("page", page));
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

        ErrorReport.send(ctx,HttpStatus.valueOf("NOT_FOUND"), "Некорректный идентификатор сайта: " + sid);
    }
}
