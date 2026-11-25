package classroomrental.service;

import classroomrental.model.Classroom;
import classroomrental.model.Equipment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClassroomServiceTest {
    private final LocalDateTime start = LocalDateTime.now().plusDays(1)
            .withHour(10).withMinute(0).withSecond(0).withNano(0);
    private final LocalDateTime end = start.plusHours(2);

    @Test
    void acceptsValidSearch() {
        assertDoesNotThrow(() -> ClassroomService.validateSearch(start, end, 20, Set.of()));
    }

    @Test
    void rejectsPastBooking() {
        assertThrows(IllegalArgumentException.class, () ->
                ClassroomService.validateSearch(start.minusDays(2), end.minusDays(2), 20, Set.of()));
    }

    @Test
    void rejectsInvalidTimeRange() {
        assertThrows(IllegalArgumentException.class, () ->
                ClassroomService.validateSearch(end, start, 20, Set.of()));
        assertThrows(IllegalArgumentException.class, () ->
                ClassroomService.validateSearch(start, start, 20, Set.of()));
    }

    @Test
    void rejectsSeconds() {
        assertThrows(IllegalArgumentException.class, () ->
                ClassroomService.validateSearch(start.plusSeconds(1), end, 20, Set.of()));
    }

    @Test
    void rejectsInvalidSearchRequirements() {
        assertThrows(IllegalArgumentException.class, () ->
                ClassroomService.validateSearch(start, end, 0, Set.of()));
        assertThrows(IllegalArgumentException.class, () ->
                ClassroomService.validateSearch(start, end, 20, null));
    }

    @Test
    void filtersConflictsCapacityEquipmentAndAccessibilityAndSorts() {
        List<Classroom> rooms = List.of(
                new Classroom(1, "Large", 40, true, Set.of(Equipment.PROJECTOR)),
                new Classroom(2, "Small", 10, true, Set.of(Equipment.PROJECTOR)),
                new Classroom(3, "Booked", 25, true, Set.of(Equipment.PROJECTOR)),
                new Classroom(4, "No projector", 25, true, Set.of()),
                new Classroom(5, "Not accessible", 25, false, Set.of(Equipment.PROJECTOR)),
                new Classroom(6, "Zulu", 20, true, Set.of(Equipment.PROJECTOR)),
                new Classroom(7, "Alpha", 20, true, Set.of(Equipment.PROJECTOR)));

        List<Long> ids = ClassroomService.filterAvailable(rooms, Set.of(3L), 20,
                        Set.of(Equipment.PROJECTOR), true)
                .stream().map(Classroom::getId).toList();

        assertEquals(List.of(7L, 6L, 1L), ids);
    }
}
