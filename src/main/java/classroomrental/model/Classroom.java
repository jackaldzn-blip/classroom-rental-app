package classroomrental.model;

import java.util.Set;

/** A training room and its available facilities. */
public class Classroom {
    private final long id;
    private final String name;
    private final int capacity;
    private final boolean accessible;
    private final Set<Equipment> equipment;

    public Classroom(long id, String name, int capacity, boolean accessible,
                     Set<Equipment> equipment) {
        if (id < 0) {
            throw new IllegalArgumentException("Classroom ID cannot be negative.");
        }
        if (name == null || name.isBlank() || name.strip().length() > 100) {
            throw new IllegalArgumentException("Classroom name must contain 1 to 100 characters.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Classroom capacity must be positive.");
        }
        if (equipment == null || equipment.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException("Equipment must not contain null values.");
        }
        this.id = id;
        this.name = name.strip();
        this.capacity = capacity;
        this.accessible = accessible;
        this.equipment = Set.copyOf(equipment);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public Set<Equipment> getEquipment() {
        return equipment;
    }

    /** Checks room facilities; booking conflicts are checked separately. */
    public boolean meetsRequirements(int minimumCapacity, Set<Equipment> requiredEquipment,
                                     boolean accessibilityRequired) {
        if (minimumCapacity <= 0 || requiredEquipment == null
                || requiredEquipment.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException("Invalid classroom search requirements.");
        }
        return capacity >= minimumCapacity
                && equipment.containsAll(requiredEquipment)
                && (!accessibilityRequired || accessible);
    }
}
