package hexlet.code.util;

import io.javalin.http.Context;

import java.time.Instant;

public class ErrorReport {
    private String title;
    private int status;
    private String description;
    private String timestamp;

    public ErrorReport(String title, int status, String description) {
        this.title = title;
        this.status = status;
        this.description = description;
        timestamp = Instant.now().toString();
    }

    public static void send(Context ctx, String title, int status, String description) {
        ErrorReport er = new ErrorReport(title, status, description);

        ctx.status(status);
        ctx.json(er);
    }
}
