package classroomrental.service;

import classroomrental.dao.BookingDao;
import classroomrental.dao.BookingUserDao;
import classroomrental.model.Booking;
import classroomrental.model.BookingUser;
import classroomrental.model.Equipment;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** Applies booking rules before saving, listing or cancelling reservations. */
public class BookingService {
    private final BookingDao bookings;
    private final BookingUserDao users;
    private final CustomerService customers;
    private final ClassroomService classrooms;

    public BookingService(BookingDao bookings, BookingUserDao users,
                          CustomerService customers, ClassroomService classrooms) {
        this.bookings = bookings;
        this.users = users;
        this.customers = customers;
        this.classrooms = classrooms;
    }

    public long createBooking(Booking booking, int minimumCapacity,
                              Set<Equipment> requiredEquipment,
                              boolean accessibilityRequired) throws SQLException {
        if (booking == null || booking.getId() != 0) {
            throw new IllegalArgumentException("Provide a new booking.");
        }
        ClassroomService.validateSearch(booking.getStartTime(), booking.getEndTime(),
                minimumCapacity, requiredEquipment);
        customers.requireCustomer(booking.getCustomerId());
        BookingUser user = users.findById(booking.getBookingUserId())
                .orElseThrow(() -> new IllegalArgumentException("Booking user not found."));
        if (user.getCustomerId() != booking.getCustomerId()) {
            throw new IllegalArgumentException("Booking user does not belong to the selected customer.");
        }
        // The DAO checks the room and conflicts again while holding a database lock.
        return bookings.create(booking, minimumCapacity, requiredEquipment, accessibilityRequired);
    }

    public List<Booking> listByClassroom(long classroomId) throws SQLException {
        classrooms.requireClassroom(classroomId);
        return bookings.findByClassroomId(classroomId);
    }

    public List<Booking> listByCustomer(long customerId) throws SQLException {
        customers.requireCustomer(customerId);
        return bookings.findByCustomerId(customerId);
    }

    public List<Booking> listUpcoming() throws SQLException {
        return bookings.findUpcoming(LocalDateTime.now());
    }

    public void cancelBooking(long bookingId) throws SQLException {
        if (bookingId <= 0 || !bookings.cancel(bookingId)) {
            throw new IllegalArgumentException("Booking not found.");
        }
    }
}
