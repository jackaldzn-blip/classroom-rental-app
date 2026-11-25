package classroomrental.dao;

import classroomrental.model.*;
import classroomrental.service.*;
import classroomrental.util.DatabaseConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in test against the local development database; uses temporary fictional records. */
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class DaoIntegrationTest {

    @Test
    void persistsCustomersRoomsAndBookingsAndRejectsConflicts() throws SQLException {
        DatabaseConnection database = DatabaseConnection.fromEnvironment();
        CustomerDao customers = new CustomerDao(database);
        BookingUserDao users = new BookingUserDao(database);
        ClassroomDao rooms = new ClassroomDao(database);
        BookingDao bookings = new BookingDao(database);
        long customerId = 0;
        long roomId = 0;
        long otherCustomerId = 0;
        String suffix = UUID.randomUUID().toString();

        try {
            customerId = customers.create(new CompanyCustomer(0,
                    "Test company " + suffix, "test@example.org"));
            Customer customer = customers.findById(customerId).orElseThrow();
            assertInstanceOf(CompanyCustomer.class, customer);
            long savedCustomerId = customerId;
            assertTrue(customers.findAll().stream().anyMatch(c -> c.getId() == savedCustomerId));

            long userId = users.create(new BookingUser(0, customerId,
                    "Test user", "user@example.org"));
            assertEquals(customerId, users.findById(userId).orElseThrow().getCustomerId());
            assertEquals(1, users.findByCustomerId(customerId).size());

            // Create a temporary room directly in SQL; the app maintains a fixed inventory.
            roomId = insertTemporaryRoom(database, "Test room " + suffix);
            rooms.update(new Classroom(roomId, "Test room " + suffix, 25, true,
                    Set.of(Equipment.PROJECTOR, Equipment.WHITEBOARD)));
            Classroom room = rooms.findById(roomId).orElseThrow();
            assertEquals(25, room.getCapacity());
            assertTrue(room.isAccessible());
            assertEquals(Set.of(Equipment.PROJECTOR, Equipment.WHITEBOARD), room.getEquipment());
            long savedRoomId = roomId;
            assertTrue(rooms.findAll().stream().anyMatch(r -> r.getId() == savedRoomId));

            LocalDateTime start = LocalDateTime.now().plusDays(30)
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusHours(2);
            Booking first = new Booking(0, roomId, customerId, userId, start, end, "Test comment");
            long firstId = bookings.create(first, 20, Set.of(Equipment.PROJECTOR), true);
            assertTrue(bookings.findConflictingClassroomIds(start, end).contains(roomId));
            assertEquals("Test comment", bookings.findByClassroomId(roomId).getFirst().getComments());

            Booking overlap = new Booking(0, roomId, customerId, userId,
                    start.plusHours(1), end.plusHours(1), null);
            assertThrows(IllegalArgumentException.class,
                    () -> bookings.create(overlap, 20, Set.of(), false));

            Booking adjacent = new Booking(0, roomId, customerId, userId, end, end.plusHours(1), null);
            long adjacentId = bookings.create(adjacent, 20, Set.of(), false);
            assertEquals(2, bookings.findByCustomerId(customerId).size());
            assertTrue(bookings.findUpcoming(LocalDateTime.now()).stream()
                    .anyMatch(b -> b.getId() == firstId));
            assertTrue(bookings.cancel(firstId));
            assertTrue(bookings.cancel(adjacentId));
            assertFalse(bookings.cancel(firstId));
            assertTrue(bookings.findByClassroomId(roomId).isEmpty());

            assertThrows(IllegalArgumentException.class,
                    () -> bookings.create(first, 26, Set.of(), false));

            CustomerService customerService = new CustomerService(customers, users);
            ClassroomService classroomService = new ClassroomService(rooms, bookings);
            BookingService bookingService = new BookingService(bookings, users,
                    customerService, classroomService);

            otherCustomerId = customerService.registerCustomer(CustomerType.INDIVIDUAL,
                    "Other test customer " + suffix, "other@example.org");
            long otherUserId = customerService.registerBookingUser(otherCustomerId,
                    "Other test user", "other@example.org");
            assertInstanceOf(IndividualCustomer.class, customerService.requireCustomer(otherCustomerId));
            assertEquals(1, customerService.listBookingUsers(otherCustomerId).size());
            assertTrue(customerService.listCustomers().stream().anyMatch(c -> c.getId() == savedCustomerId));

            classroomService.updateClassroom(roomId, "Test room " + suffix, 30, true,
                    Set.of(Equipment.PROJECTOR));
            assertEquals(30, classroomService.requireClassroom(roomId).getCapacity());
            assertTrue(classroomService.listClassrooms().stream().anyMatch(r -> r.getId() == savedRoomId));
            assertTrue(classroomService.searchAvailable(start, end, 30, Set.of(Equipment.PROJECTOR), true)
                    .stream().anyMatch(r -> r.getId() == savedRoomId));

            Booking mismatchedUser = new Booking(0, roomId, customerId, otherUserId, start, end, null);
            assertThrows(IllegalArgumentException.class,
                    () -> bookingService.createBooking(mismatchedUser, 20, Set.of(), false));
            Booking past = new Booking(0, roomId, customerId, userId,
                    start.minusDays(60), end.minusDays(60), null);
            assertThrows(IllegalArgumentException.class,
                    () -> bookingService.createBooking(past, 20, Set.of(), false));

            long serviceBookingId = bookingService.createBooking(first, 30,
                    Set.of(Equipment.PROJECTOR), true);
            assertFalse(classroomService.searchAvailable(start, end, 30, Set.of(Equipment.PROJECTOR), true)
                    .stream().anyMatch(r -> r.getId() == savedRoomId));
            assertEquals(serviceBookingId, bookingService.listByClassroom(roomId).getFirst().getId());
            assertEquals(serviceBookingId, bookingService.listByCustomer(customerId).getFirst().getId());
            assertTrue(bookingService.listUpcoming().stream().anyMatch(b -> b.getId() == serviceBookingId));
            assertThrows(IllegalArgumentException.class,
                    () -> bookingService.createBooking(overlap, 20, Set.of(), false));
            bookingService.cancelBooking(serviceBookingId);
            assertThrows(IllegalArgumentException.class, () -> bookingService.cancelBooking(serviceBookingId));
        } finally {
            // Delete only records belonging to this test, in foreign-key order.
            try (Connection connection = database.open()) {
                if (customerId > 0) {
                    delete(connection, "DELETE FROM bookings WHERE customer_id = ?", customerId);
                    delete(connection, "DELETE FROM booking_users WHERE customer_id = ?", customerId);
                    delete(connection, "DELETE FROM customers WHERE id = ?", customerId);
                }
                if (otherCustomerId > 0) {
                    delete(connection, "DELETE FROM bookings WHERE customer_id = ?", otherCustomerId);
                    delete(connection, "DELETE FROM booking_users WHERE customer_id = ?", otherCustomerId);
                    delete(connection, "DELETE FROM customers WHERE id = ?", otherCustomerId);
                }
                if (roomId > 0) {
                    delete(connection, "DELETE FROM classroom_equipment WHERE classroom_id = ?", roomId);
                    delete(connection, "DELETE FROM classrooms WHERE id = ?", roomId);
                }
            }
        }
    }

    private long insertTemporaryRoom(DatabaseConnection database, String name) throws SQLException {
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

    private void delete(Connection connection, String sql, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }
}
