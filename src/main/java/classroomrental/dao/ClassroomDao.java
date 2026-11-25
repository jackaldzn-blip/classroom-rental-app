package classroomrental.dao;

import classroomrental.model.Classroom;
import classroomrental.model.Equipment;
import classroomrental.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Reads and maintains classrooms and their equipment. */
public class ClassroomDao {
    private final DatabaseConnection database;

    public ClassroomDao(DatabaseConnection database) {
        this.database = database;
    }

    public List<Classroom> findAll() throws SQLException {
        String sql = "SELECT id, name, capacity, is_accessible FROM classrooms ORDER BY name, id";
        List<Classroom> rooms = new ArrayList<>();
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rooms.add(map(connection, result, false));
            }
        }
        return rooms;
    }

    public Optional<Classroom> findById(long id) throws SQLException {
        try (Connection connection = database.open()) {
            return findById(connection, id, false);
        }
    }

    /** Reuse the caller's connection when a booking needs to lock the room. */
    static Optional<Classroom> findById(Connection connection, long id, boolean lock)
            throws SQLException {
        String sql = "SELECT id, name, capacity, is_accessible FROM classrooms WHERE id = ?"
                + (lock ? " FOR UPDATE" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(connection, result, lock)) : Optional.empty();
            }
        }
    }

    public void update(Classroom room) throws SQLException {
        try (Connection connection = database.open()) {
            connection.setAutoCommit(false);
            try {
                if (findById(connection, room.getId(), true).isEmpty()) {
                    throw new IllegalArgumentException("Classroom not found.");
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE classrooms SET name = ?, capacity = ?, is_accessible = ? WHERE id = ?")) {
                    statement.setString(1, room.getName());
                    statement.setInt(2, room.getCapacity());
                    statement.setBoolean(3, room.isAccessible());
                    statement.setLong(4, room.getId());
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM classroom_equipment WHERE classroom_id = ?")) {
                    statement.setLong(1, room.getId());
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO classroom_equipment (classroom_id, equipment) VALUES (?, ?)")) {
                    for (Equipment item : room.getEquipment()) {
                        statement.setLong(1, room.getId());
                        statement.setString(2, item.name());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
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

    private static Classroom map(Connection connection, ResultSet result, boolean lock)
            throws SQLException {
        long id = result.getLong("id");
        String name = result.getString("name");
        int capacity = result.getInt("capacity");
        boolean accessible = result.getBoolean("is_accessible");
        Set<Equipment> equipment = EnumSet.noneOf(Equipment.class);
        String sql = "SELECT equipment FROM classroom_equipment WHERE classroom_id = ?"
                + (lock ? " FOR UPDATE" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet items = statement.executeQuery()) {
                while (items.next()) {
                    equipment.add(Equipment.valueOf(items.getString("equipment")));
                }
            }
        }
        return new Classroom(id, name, capacity, accessible, equipment);
    }
}
