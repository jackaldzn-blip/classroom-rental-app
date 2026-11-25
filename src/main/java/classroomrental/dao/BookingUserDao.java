package classroomrental.dao;

import classroomrental.model.BookingUser;
import classroomrental.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores booking users and their customer relationships. */
public class BookingUserDao {
    private final DatabaseConnection database;

    public BookingUserDao(DatabaseConnection database) {
        this.database = database;
    }

    public long create(BookingUser user) throws SQLException {
        String sql = "INSERT INTO booking_users (customer_id, name, email) VALUES (?, ?, ?)";
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, user.getCustomerId());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("Booking user ID was not generated.");
            }
        }
    }

    public Optional<BookingUser> findById(long id) throws SQLException {
        String sql = "SELECT id, customer_id, name, email FROM booking_users WHERE id = ?";
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public List<BookingUser> findByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT id, customer_id, name, email FROM booking_users WHERE customer_id = ? ORDER BY name, id";
        List<BookingUser> users = new ArrayList<>();
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    users.add(map(result));
                }
            }
        }
        return users;
    }

    private BookingUser map(ResultSet result) throws SQLException {
        return new BookingUser(result.getLong("id"), result.getLong("customer_id"),
                result.getString("name"), result.getString("email"));
    }
}
