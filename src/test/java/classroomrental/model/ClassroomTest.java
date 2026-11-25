package classroomrental.model;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ClassroomTest {
    private final Classroom room = new Classroom(1, "Room 1", 20, true,
            Set.of(Equipment.WHITEBOARD, Equipment.PROJECTOR));

    @Test
    void matchesAllRequirements() {
        assertTrue(room.meetsRequirements(20, Set.of(Equipment.PROJECTOR), true));
        assertTrue(room.meetsRequirements(1, Set.of(), false));
    }

    @Test
    void rejectsInsufficientCapacity() {
        assertFalse(room.meetsRequirements(21, Set.of(), false));
    }

    @Test
    void rejectsMissingEquipment() {
        assertFalse(room.meetsRequirements(10,
                Set.of(Equipment.PROJECTOR, Equipment.COMPUTERS), false));
    }

    @Test
    void checksAccessibilityOnlyWhenRequested() {
        Classroom other = new Classroom(2, "Room 2", 20, false, Set.of());
        assertFalse(other.meetsRequirements(10, Set.of(), true));
        assertTrue(other.meetsRequirements(10, Set.of(), false));
    }

    @Test
    void rejectsInvalidCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> new Classroom(1, "Room 1", 0, true, Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> room.meetsRequirements(0, Set.of(), false));
    }

    @Test
    void protectsEquipmentFromExternalModification() {
        Set<Equipment> items = new HashSet<>();
        items.add(Equipment.PROJECTOR);
        Classroom other = new Classroom(2, "Room 2", 20, false, items);
        items.clear();
        assertEquals(Set.of(Equipment.PROJECTOR), other.getEquipment());
        assertThrows(UnsupportedOperationException.class,
                () -> other.getEquipment().add(Equipment.WHITEBOARD));
    }
}
