package classroomrental.model;

import java.time.LocalDateTime;

/** A reservation connecting a classroom, customer and booking user. */
public class Booking {
    private final long id;
    private final long classroomId;
    private final long customerId;
    private final long bookingUserId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String comments;

    public Booking(long id, long classroomId, long customerId, long bookingUserId,
                   LocalDateTime startTime, LocalDateTime endTime, String comments) {
        if (id < 0 || classroomId <= 0 || customerId <= 0 || bookingUserId <= 0) {
            throw new IllegalArgumentException("Booking requires valid classroom, customer and booking user IDs.");
        }
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Booking start must be before its end.");
        }
        if (startTime.getSecond() != 0 || startTime.getNano() != 0
                || endTime.getSecond() != 0 || endTime.getNano() != 0) {
            throw new IllegalArgumentException("Booking times must use minute precision.");
        }
        if (comments != null && comments.strip().length() > 1000) {
            throw new IllegalArgumentException("Booking comments cannot exceed 1000 characters.");
        }
        this.id = id;
        this.classroomId = classroomId;
        this.customerId = customerId;
        this.bookingUserId = bookingUserId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.comments = comments == null || comments.isBlank() ? null : comments.strip();
    }

    public long getId() {
        return id;
    }

    public long getClassroomId() {
        return classroomId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public long getBookingUserId() {
        return bookingUserId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getComments() {
        return comments;
    }

    /** Adjacent time ranges do not overlap. Room identity is checked separately. */
    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Requested start must be before its end.");
        }
        return startTime.isBefore(end) && endTime.isAfter(start);
    }
}
