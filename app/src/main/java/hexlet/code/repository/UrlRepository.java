package hexlet.code.repository;

import hexlet.code.model.Url;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UrlRepository extends BaseRepository {

    private static List<Url> getEntitiesInDescendingOrder() {
        List<Url> urls = new LinkedList<>();

        String sql = "SELECT * FROM urls ORDER BY urls.created_at DESC";

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

    public static List<Url> getEntities() {
        return getEntitiesInDescendingOrder();
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

    public static Optional<Url> find(Long id) throws SQLException {
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
        }

        return Optional.empty();
    }

    public static Optional<Url> search(String url) throws SQLException {
        String sql = "SELECT * FROM urls WHERE urls.name = ?";

        try (var connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, url);

            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                var id = resultSet.getLong("id");
                var createdAt = resultSet.getTimestamp("created_at").toInstant();

                return Optional.of(new Url(id, url, createdAt));
            }
        }

        return Optional.empty();
    }

    public static boolean delete(Long id) throws SQLException {
        var sql = "DELETE FROM urls WHERE urls.id = ?";

        try (var connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);

            int rowsDeleted = preparedStatement.executeUpdate();

            if (rowsDeleted > 0) {
                return true;
            }
        }

        return false;
    }

    public static int deleteAll() throws SQLException {
        var sql = "DELETE FROM urls";

        try (var connection = dataSource.getConnection()) {
            var statement = connection.createStatement();

            int rowsDeleted = statement.executeUpdate(sql);

            return rowsDeleted;
        }
    }
}
