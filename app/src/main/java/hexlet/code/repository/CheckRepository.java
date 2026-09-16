package hexlet.code.repository;

import hexlet.code.model.UrlCheck;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckRepository extends BaseRepository {
    public static List<UrlCheck> getEntities() {
        List<UrlCheck> urlChecks = new LinkedList<>();

        String sql = "SELECT * FROM url_checks";

        try (var connection = dataSource.getConnection()) {
            var statement = connection.createStatement();

            var resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                var id = resultSet.getLong(1);
                var urlId = resultSet.getLong("url_id");
                var statusCode = resultSet.getInt("status_code");
                var h1 = resultSet.getString("h1");
                var title = resultSet.getString("title");
                var description = resultSet.getString("description");
                var createdAt = resultSet.getTimestamp("created_at").toInstant();

                var urlCheck =
                        new UrlCheck(id, statusCode, title, h1, description, urlId, createdAt);

                urlChecks.add(urlCheck);
            }

        } catch (SQLException ex) {
            log.error("CheckRepository::getEntities() error: {}", ex.getMessage());
        }

        return urlChecks;
    }

    public static List<UrlCheck> getAllChecksForUrl(long urlId) {
        List<UrlCheck> urlChecks = new LinkedList<>();

        String sql = "SELECT * FROM url_checks WHERE url_id = ?";

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

    public static UrlCheck getLastCheckForUrl(long urlId) {
        String sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC LIMIT 1";

        try (var connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, urlId);

            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                var id = resultSet.getLong(1);
                var uid = resultSet.getLong("url_id");
                var statusCode = resultSet.getInt("status_code");
                var h1 = resultSet.getString("h1");
                var title = resultSet.getString("title");
                var description = resultSet.getString("description");
                var createdAt = resultSet.getTimestamp("created_at").toInstant();

                var urlCheck = new UrlCheck(id, statusCode, title, h1, description, uid, createdAt);

                return urlCheck;
            }

        } catch (SQLException ex) {
            log.error("CheckRepository::getLastCheckForUrl() error: {}", ex.getMessage());
        }

        return null;
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
