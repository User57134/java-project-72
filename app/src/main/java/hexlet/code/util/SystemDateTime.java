package hexlet.code.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SystemDateTime {
    public static String format(Instant dateTime) {
        ZonedDateTime zdt = dateTime.atZone(ZoneId.systemDefault());

        return zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
