package classroomrental.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class BookingTest {
    private final LocalDateTime start = LocalDateTime.of(2030, 1, 15, 10, 0);
    private final LocalDateTime end = start.plusHours(2);
    private final Booking booking = new Booking(1, 1, 2, 3, start, end, null);

    @Test
    void detectsPartialOverlap() {
        assertTrue(booking.overlaps(start.minusHours(1), start.plusHours(1)));
        assertTrue(booking.overlaps(end.minusHours(1), end.plusHours(1)));
    }

    @Test
    void detectsIdenticalAndContainedRanges() {
        assertTrue(booking.overlaps(start, end));
        assertTrue(booking.overlaps(start.plusMinutes(15), end.minusMinutes(15)));
        assertTrue(booking.overlaps(start.minusHours(1), end.plusHours(1)));
    }

    @Test
    void allowsAdjacentBookings() {
        assertFalse(booking.overlaps(start.minusHours(1), start));
        assertFalse(booking.overlaps(end, end.plusHours(1)));
    }

    @Test
    void allowsSeparatedTimeRanges() {
        assertFalse(booking.overlaps(end.plusHours(1), end.plusHours(2)));
    }

    @Test
    void rejectsInvalidTimeRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> new Booking(0, 1, 2, 3, end, start, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Booking(0, 1, 2, 3, start, start, null));
        assertThrows(IllegalArgumentException.class,
                () -> booking.overlaps(end, start));
    }

    @Test
    void rejectsInvalidRelatedIdsAndSeconds() {
        assertThrows(IllegalArgumentException.class,
                () -> new Booking(0, 0, 2, 3, start, end, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Booking(0, 1, 2, 3, start.plusSeconds(1), end, null));
    }

    @Test
    void acceptsOptionalComments() {
        assertNull(booking.getComments());
        assertNull(new Booking(0, 1, 2, 3, start, end, " ").getComments());
        assertEquals("Bring laptops",
                new Booking(0, 1, 2, 3, start, end, " Bring laptops ").getComments());
    }

    @Test
    void canReadHistoricalBookings() {
        LocalDateTime historicalStart = LocalDateTime.of(2000, 1, 1, 10, 0);
        assertDoesNotThrow(() -> new Booking(1, 1, 2, 3,
                historicalStart, historicalStart.plusHours(1), null));
    }
}
