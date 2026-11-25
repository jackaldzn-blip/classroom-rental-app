package classroomrental.ui;

import classroomrental.dao.*;
import classroomrental.model.Booking;
import classroomrental.model.Customer;
import classroomrental.model.CustomerType;
import classroomrental.service.*;
import classroomrental.util.DatabaseConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises console commands, services and real MySQL persistence together. */
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class ConsoleIntegrationTest {
    private final DatabaseConnection database = DatabaseConnection.fromEnvironment();
    private final CustomerDao customers = new CustomerDao(database);
    private final BookingUserDao users = new BookingUserDao(database);
    private final BookingDao bookings = new BookingDao(database);

    @Test
    void registersCustomerAndCompletesBookingWorkflowThroughConsole() throws SQLException {
        String suffix = UUID.randomUUID().toString();
        String customerName = "Console test customer " + suffix;
        String roomName = "Console test room " + suffix;
        long roomId = 0;

        try {
            roomId = insertRoom(roomName);
            String roomLine = "ID " + roomId + " | " + roomName + " |";

            String registration = runMenu("1", "2", customerName, "console@example.org", "0");
            assertTrue(registration.contains("Customer registered. ID:"), registration);
            Customer customer = customers.findAll().stream()
                    .filter(c -> c.getName().equals(customerName)).findFirst().orElseThrow();
            assertEquals(CustomerType.COMPANY, customer.getType());
            long customerId = customer.getId();

            String userRegistration = runMenu("2", Long.toString(customerId),
                    "Console test user", "user@example.org", "0");
            assertTrue(userRegistration.contains("Booking user registered. ID:"), userRegistration);
            long userId = users.findByCustomerId(customerId).getFirst().getId();

            String update = runMenu("4", Long.toString(roomId), roomName,
                    "999", "y", "1,2", "0");
            assertTrue(update.contains("Classroom updated."), update);

            LocalDateTime start = LocalDateTime.now().plusDays(30)
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm");
            String from = start.format(format);
            String to = start.plusHours(2).format(format);

            String available = runMenu("5", from, to, "999", "1,2", "y", "0");
            assertTrue(available.contains(roomLine), available);

            String created = runMenu("6", from, to, "999", "1,2", "y",
                    Long.toString(roomId), Long.toString(customerId), Long.toString(userId),
                    "Bring laptops", "0");
            assertTrue(created.contains("Booking created. ID:"), created);
            Booking booking = bookings.findByCustomerId(customerId).getFirst();
            assertEquals(roomId, booking.getClassroomId());
            assertEquals("Bring laptops", booking.getComments());

            String unavailable = runMenu("5", from, to, "999", "1,2", "y", "0");
            assertFalse(unavailable.contains(roomLine), unavailable);

            String bookingLine = "ID " + booking.getId() + " | Room ID: " + roomId;
            assertTrue(runMenu("7", Long.toString(roomId), "0").contains(bookingLine));
            assertTrue(runMenu("8", Long.toString(customerId), "0").contains(bookingLine));
            assertTrue(runMenu("9", "0").contains(bookingLine));

            String abandoned = runMenu("10", Long.toString(booking.getId()), "n", "0");
            assertTrue(abandoned.contains("Cancellation abandoned."));
            assertEquals(1, bookings.findByCustomerId(customerId).size());

            String cancelled = runMenu("10", Long.toString(booking.getId()), "y", "0");
            assertTrue(cancelled.contains("Booking cancelled."));
            assertTrue(bookings.findByCustomerId(customerId).isEmpty());
            assertTrue(runMenu("5", from, to, "999", "1,2", "y", "0").contains(roomLine));
        } finally {
            // Remove only the uniquely named records created by this test.
            try (Connection connection = database.open()) {
                delete(connection, "DELETE b FROM bookings b JOIN customers c ON b.customer_id = c.id WHERE c.name = ?", customerName);
                delete(connection, "DELETE u FROM booking_users u JOIN customers c ON u.customer_id = c.id WHERE c.name = ?", customerName);
                delete(connection, "DELETE FROM customers WHERE name = ?", customerName);
                if (roomId > 0) {
                    delete(connection, "DELETE FROM classroom_equipment WHERE classroom_id = ?", roomId);
                    delete(connection, "DELETE FROM classrooms WHERE id = ?", roomId);
                }
            }
        }
    }

    private String runMenu(String... commands) {
        // Recreate the service objects for each simulated console session.
        CustomerService customerService = new CustomerService(customers, users);
        ClassroomService classroomService = new ClassroomService(new ClassroomDao(database), bookings);
        BookingService bookingService = new BookingService(bookings, users, customerService, classroomService);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(String.join("\n", commands) + "\n");
             PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            new ConsoleMenu(customerService, classroomService, bookingService, input, output).run();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private long insertRoom(String name) throws SQLException {
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO classrooms (name, capacity, is_accessible) VALUES (?, 10, FALSE)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("Test classroom ID was not generated.");
            }
        }
    }

    private void delete(Connection connection, String sql, Object value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            statement.executeUpdate();
        }
    }
}
