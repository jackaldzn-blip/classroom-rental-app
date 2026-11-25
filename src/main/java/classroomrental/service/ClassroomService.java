package classroomrental.service;

import classroomrental.dao.BookingDao;
import classroomrental.dao.ClassroomDao;
import classroomrental.model.Classroom;
import classroomrental.model.Equipment;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Maintains classroom details and finds suitable, available rooms. */
public class ClassroomService {
    private final ClassroomDao rooms;
    private final BookingDao bookings;

    public ClassroomService(ClassroomDao rooms, BookingDao bookings) {
        this.rooms = rooms;
        this.bookings = bookings;
    }

    public List<Classroom> listClassrooms() throws SQLException {
        return rooms.findAll();
    }

    public Classroom requireClassroom(long id) throws SQLException {
        return rooms.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found."));
    }

    public void updateClassroom(long id, String name, int capacity, boolean accessible,
                                Set<Equipment> equipment) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Choose a valid classroom ID.");
        }
        rooms.update(new Classroom(id, name, capacity, accessible, equipment));
    }

    public List<Classroom> searchAvailable(LocalDateTime start, LocalDateTime end,
                                           int minimumCapacity, Set<Equipment> requiredEquipment,
                                           boolean accessibilityRequired) throws SQLException {
        validateSearch(start, end, minimumCapacity, requiredEquipment);
        Set<Long> conflicts = bookings.findConflictingClassroomIds(start, end);
        return filterAvailable(rooms.findAll(), conflicts, minimumCapacity,
                requiredEquipment, accessibilityRequired);
    }

    /** Shared validation for searching and creating reservations. */
    static void validateSearch(LocalDateTime start, LocalDateTime end,
                               int minimumCapacity, Set<Equipment> equipment) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Booking start must be before its end.");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New bookings cannot start in the past.");
        }
        if (start.getSecond() != 0 || start.getNano() != 0
                || end.getSecond() != 0 || end.getNano() != 0) {
            throw new IllegalArgumentException("Booking times must use minute precision.");
        }
        if (minimumCapacity <= 0 || equipment == null
                || equipment.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException("Invalid classroom search requirements.");
        }
    }

    /** Filter and sort in Java to demonstrate collections, streams and lambdas. */
    static List<Classroom> filterAvailable(List<Classroom> rooms, Set<Long> conflicts,
                                           int minimumCapacity, Set<Equipment> equipment,
                                           boolean accessibilityRequired) {
        return rooms.stream()
                .filter(room -> !conflicts.contains(room.getId()))
                .filter(room -> room.meetsRequirements(minimumCapacity, equipment,
                        accessibilityRequired))
                .sorted(Comparator.comparingInt(Classroom::getCapacity)
                        .thenComparing(Classroom::getName)
                        .thenComparingLong(Classroom::getId))
                .toList();
    }
}
