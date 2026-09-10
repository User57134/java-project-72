package hexlet.code.util;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.time.Instant;

public class ErrorReport {

    public static void send(Context ctx, HttpStatus status, String description) {
        Instant timestamp = Instant.now();

        ctx.status(status);
        ctx.header("Content-Type", "application/json; charset=UTF-8");
        ctx.result(timestamp.toString() + " Error: " + description);
    }
}
