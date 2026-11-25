package classroomrental.model;

/** A person who creates a reservation on behalf of a customer. */
public class BookingUser {
    private final long id;
    private final long customerId;
    private final String name;
    private final String email;

    public BookingUser(long id, long customerId, String name, String email) {
        if (id < 0 || customerId <= 0) {
            throw new IllegalArgumentException("A booking user requires a valid customer ID.");
        }
        if (name == null || name.isBlank() || name.strip().length() > 150) {
            throw new IllegalArgumentException("Booking user name must contain 1 to 150 characters.");
        }
        if (email == null || email.isBlank() || email.strip().length() > 254) {
            throw new IllegalArgumentException("Booking user email must contain 1 to 254 characters.");
        }
        this.id = id;
        this.customerId = customerId;
        this.name = name.strip();
        this.email = email.strip();
    }

    public long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
