package classroomrental.dao;

import classroomrental.model.Booking;
import classroomrental.model.Classroom;
import classroomrental.model.Equipment;
import classroomrental.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Persists reservations and prevents concurrent overlapping bookings. */
public class BookingDao {
    private final DatabaseConnection database;

    public BookingDao(DatabaseConnection database) {
        this.database = database;
    }

    public Set<Long> findConflictingClassroomIds(LocalDateTime start, LocalDateTime end)
            throws SQLException {
        String sql = "SELECT DISTINCT classroom_id FROM bookings WHERE start_time < ? AND end_time > ?";
        Set<Long> ids = new HashSet<>();
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, end);
            statement.setObject(2, start);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ids.add(result.getLong("classroom_id"));
                }
            }
        }
        return ids;
    }

    /**
     * Lock the classroom before checking availability and inserting.
     * Every application booking follows this transaction, so simultaneous
     * reservations for the same classroom cannot bypass the overlap check.
     */
    public long create(Booking booking, int minimumCapacity, Set<Equipment> requiredEquipment,
                       boolean accessibilityRequired) throws SQLException {
        try (Connection connection = database.open()) {
            connection.setAutoCommit(false);
            try {
                Classroom room = ClassroomDao.findById(connection, booking.getClassroomId(), true)
                        .orElseThrow(() -> new IllegalArgumentException("Classroom not found."));
                if (!room.meetsRequirements(minimumCapacity, requiredEquipment, accessibilityRequired)) {
                    throw new IllegalArgumentException("Classroom no longer meets the requirements.");
                }

                String conflictSql = """
                        SELECT id FROM bookings
                        WHERE classroom_id = ? AND start_time < ? AND end_time > ?
                        FOR UPDATE
                        """;
                try (PreparedStatement statement = connection.prepareStatement(conflictSql)) {
                    statement.setLong(1, booking.getClassroomId());
                    statement.setObject(2, booking.getEndTime());
                    statement.setObject(3, booking.getStartTime());
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            throw new IllegalArgumentException("Classroom is already booked for this time.");
                        }
                    }
                }

                String insertSql = """
                        INSERT INTO bookings (classroom_id, customer_id, booking_user_id,
                                              start_time, end_time, comments)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;
                long id;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setLong(1, booking.getClassroomId());
                    statement.setLong(2, booking.getCustomerId());
                    statement.setLong(3, booking.getBookingUserId());
                    statement.setObject(4, booking.getStartTime());
                    statement.setObject(5, booking.getEndTime());
                    statement.setString(6, booking.getComments());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Booking ID was not generated.");
                        }
                        id = keys.getLong(1);
                    }
                }
                connection.commit();
                return id;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    exception.addSuppressed(rollbackError);
                }
                throw exception;
            }
        }
    }

    public List<Booking> findByClassroomId(long classroomId) throws SQLException {
        return findBookings("classroom_id = ?", classroomId);
    }

    public List<Booking> findByCustomerId(long customerId) throws SQLException {
        return findBookings("customer_id = ?", customerId);
    }

    public List<Booking> findUpcoming(LocalDateTime now) throws SQLException {
        return findBookings("start_time >= ?", now);
    }

    private List<Booking> findBookings(String condition, Object parameter) throws SQLException {
        // The condition comes only from the fixed queries above, not user input.
        String sql = "SELECT * FROM bookings WHERE " + condition + " ORDER BY start_time, id";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    bookings.add(new Booking(result.getLong("id"),
                            result.getLong("classroom_id"), result.getLong("customer_id"),
                            result.getLong("booking_user_id"),
                            result.getObject("start_time", LocalDateTime.class),
                            result.getObject("end_time", LocalDateTime.class),
                            result.getString("comments")));
                }
            }
        }
        return bookings;
    }

    public boolean cancel(long id) throws SQLException {
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM bookings WHERE id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }
}
