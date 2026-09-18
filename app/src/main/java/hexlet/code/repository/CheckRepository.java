package hexlet.code.repository;

import hexlet.code.model.UrlCheck;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckRepository extends BaseRepository {

    public static Map<Long, UrlCheck> getLatestChecksByUrl() {
        Map<Long, UrlCheck> lastChecks = new HashMap<>();

        String sql =
                "SELECT DISTINCT ON (url_id) * FROM url_checks ORDER BY url_id, created_at DESC";

        try (var connection = dataSource.getConnection()) {
            var statement = connection.createStatement();

            var resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var urlId = resultSet.getLong("url_id");
                var statusCode = resultSet.getInt("status_code");
                var h1 = resultSet.getString("h1");
                var title = resultSet.getString("title");
                var description = resultSet.getString("description");
                var createdAt = resultSet.getTimestamp("created_at").toInstant();

                var urlCheck =
                        new UrlCheck(id, statusCode, title, h1, description, urlId, createdAt);

                lastChecks.put(urlId, urlCheck);
            }

        } catch (SQLException ex) {
            log.error("CheckRepository::getEntities() error: {}", ex.getMessage());
        }

        return lastChecks;
    }

    public static List<UrlCheck> getAllChecksForUrl(long urlId) {
        List<UrlCheck> urlChecks = new LinkedList<>();

        String sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC";

        try (var connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, urlId);

            var resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                var id = resultSet.getLong(1);
                var uid = resultSet.getLong("url_id");
                var statusCode = resultSet.getInt("status_code");
                var h1 = resultSet.getString("h1");
                var title = resultSet.getString("title");
                var description = resultSet.getString("description");
                var createdAt = resultSet.getTimestamp("created_at").toInstant();

                var urlCheck = new UrlCheck(id, statusCode, title, h1, description, uid, createdAt);

                urlChecks.add(urlCheck);
            }

        } catch (SQLException ex) {
            log.error("CheckRepository::getEntities() error: {}", ex.getMessage());
        }

        return urlChecks;
    }

    public static long save(UrlCheck check) {
        String sql =
                "INSERT INTO url_checks (url_id, status_code, h1, title, description, created_at) VALUES(?, ?, ?, ?, ?, ?)";
        Long id = null;

        try (var connection = dataSource.getConnection()) {
            var preparedStatement =
                    connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setLong(1, check.getUrlId());
            preparedStatement.setInt(2, check.getStatusCode());
            preparedStatement.setString(3, check.getH1());
            preparedStatement.setString(4, check.getTitle());
            preparedStatement.setString(5, check.getDescription());

            if (check.getCreatedAt() == null) {
                check.setCreatedAt(Instant.now());
            }
            preparedStatement.setTimestamp(6, Timestamp.from(check.getCreatedAt()));

            preparedStatement.executeUpdate();

            var generatedKey = preparedStatement.getGeneratedKeys();
            if (generatedKey.next()) {
                id = generatedKey.getLong(1);
                check.setId(id);
                return id;
            } else {
                log.error(
                        "DB has not returned an id after saving a check for the url: "
                                + check.getUrlId());
                return 0L;
            }

        } catch (SQLException ex) {
            log.error("CheckRepository::save() error: {}", ex.getMessage());
            return 0L;
        }
    }
}
