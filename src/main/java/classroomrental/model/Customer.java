package classroomrental.model;

/** Common details shared by individual and company customers. */
public abstract class Customer {
    private final long id;
    private final String name;
    private final String email;

    protected Customer(long id, String name, String email) {
        if (id < 0) {
            throw new IllegalArgumentException("Customer ID cannot be negative.");
        }
        if (name == null || name.isBlank() || name.strip().length() > 150) {
            throw new IllegalArgumentException("Customer name must contain 1 to 150 characters.");
        }
        if (email == null || email.isBlank() || email.strip().length() > 254) {
            throw new IllegalArgumentException("Customer email must contain 1 to 254 characters.");
        }
        this.id = id;
        this.name = name.strip();
        this.email = email.strip();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public abstract CustomerType getType();
}
