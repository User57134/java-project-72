package hexlet.code.repository;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {

    public static List<Url> getEntities() {
        List<Url> urls = new LinkedList<>();

        String sql = "SELECT * FROM urls";

        try (var connection = dataSource.getConnection()) {
            var statement = connection.createStatement();

            var resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                var id = resultSet.getLong(1);
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at").toInstant();
                var url = new Url(id, name, createdAt);

                urls.add(url);
            }

        } catch (SQLException ex) {
            log.error("UrlRepository::getEntities() error: {}", ex.getMessage());
        }

        return urls;
    }

    public static long save(Url url) {
        String sql = "INSERT INTO urls (name, created_at) VALUES(?, ?)";
        Long id = null;

        try (var connection = dataSource.getConnection()) {
            var preparedStatement =
                    connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, url.getName());

            Instant createdAt = Instant.now();
            url.setCreatedAt(createdAt);
            preparedStatement.setTimestamp(2, Timestamp.from(url.getCreatedAt()));

            preparedStatement.executeUpdate();

            var generatedKey = preparedStatement.getGeneratedKeys();
            if (generatedKey.next()) {
                id = generatedKey.getLong(1);
                url.setId(id);
                return id;
            } else {
                log.error("DB has not returned an id after saving the url: " + url.getName());
                return 0L;
            }

        } catch (SQLException ex) {
            log.error("UrlRepository::save() error: {}", ex.getMessage());
            return 0L;
        }
    }

    public static long saveCheck(UrlCheck check) {
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

            Instant createdAt = Instant.now();
            check.setCreatedAt(createdAt);
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
            log.error("UrlRepository::saveCheck() error: {}", ex.getMessage());
            return 0L;
        }
    }

    public static Optional<Url> find(Long id) {
        String sql = "SELECT * FROM urls WHERE id = ?";

        try (var connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);

            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at").toInstant();

                return Optional.of(new Url(id, name, createdAt));
            }
        } catch (SQLException ex) {
            log.error("UrlRepository::find() error: {}", ex.getMessage());
        }

        return Optional.empty();
    }

    public static long search(String url) {
        String sql = "SELECT * FROM urls WHERE urls.name = ?";

        try (var connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, url);

            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException ex) {
            log.error("UrlRepository::contains() error: {}", ex.getMessage());
        }

        return 0L;
    }

    public static boolean delete(Long id) {
        var sql = "DELETE FROM urls WHERE urls.id = ?";

        try (var connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);

            int rowsDeleted = preparedStatement.executeUpdate();

            if (rowsDeleted > 0) {
                return true;
            }
        } catch (SQLException ex) {
            log.error("UrlRepository::delete() error: {}", ex.getMessage());
        }

        return false;
    }

    public static int deleteAll() {
        var sql = "DELETE FROM urls";

        try (var connection = dataSource.getConnection()) {
            var statement = connection.createStatement();

            int rowsDeleted = statement.executeUpdate(sql);

            return rowsDeleted;

        } catch (SQLException ex) {
            log.error("UrlRepository::deleteAll() error: {}", ex.getMessage());
        }

        return 0;
    }
}
