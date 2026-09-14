package hexlet.code.repository;

import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseRepository {
    protected static final Logger log = LoggerFactory.getLogger(App.class);

    public static HikariDataSource dataSource;
}
