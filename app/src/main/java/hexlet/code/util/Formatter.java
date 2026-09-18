package hexlet.code.util;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Formatter {
    public static String makeTimestampAsString(Instant dateTime) {
        ZonedDateTime zdt = dateTime.atZone(ZoneId.systemDefault());

        return zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static Optional<URL> getBaseUrl(String site) {
        try {
            var url = new URI(site).toURL();
            var protocol = url.getProtocol();
            var host = url.getHost();
            var port = url.getPort();

            return Optional.of(
                    URI.create(protocol + "://" + host + ((port != -1) ? (":" + port) : ""))
                            .toURL());
        } catch (Exception ex) {
            log.error("Некорректный формат URL: {}", site);
            return Optional.empty();
        }
    }
}
