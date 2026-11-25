package classroomrental.ui;

import classroomrental.model.*;
import classroomrental.service.BookingService;
import classroomrental.service.ClassroomService;
import classroomrental.service.CustomerService;

import java.io.PrintStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/** Displays the console menu and passes user requests to the services. */
public class ConsoleMenu {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final CustomerService customers;
    private final ClassroomService classrooms;
    private final BookingService bookings;
    private final Scanner input;
    private final PrintStream output;

    public ConsoleMenu(CustomerService customers, ClassroomService classrooms,
                       BookingService bookings, Scanner input, PrintStream output) {
        this.customers = customers;
        this.classrooms = classrooms;
        this.bookings = bookings;
        this.input = input;
        this.output = output;
    }

    public void run() {
        while (true) {
            printMenu();
            try {
                switch (read("Select an option: ")) {
                    case "1" -> registerCustomer();
                    case "2" -> registerBookingUser();
                    case "3" -> printClassrooms(classrooms.listClassrooms());
                    case "4" -> updateClassroom();
                    case "5" -> searchAvailable();
                    case "6" -> createBooking();
                    case "7" -> viewByClassroom();
                    case "8" -> viewByCustomer();
                    case "9" -> printBookings(bookings.listUpcoming());
                    case "10" -> cancelBooking();
                    case "0" -> {
                        output.println("Goodbye!");
                        return;
                    }
                    default -> output.println("Invalid menu option. Choose 0 to 10.");
                }
            } catch (NoSuchElementException exception) {
                output.println("Input closed. Goodbye!");
                return;
            } catch (IllegalArgumentException exception) {
                output.println("Error: " + exception.getMessage());
            } catch (SQLException exception) {
                output.println("Database operation failed (SQL state: "
                        + exception.getSQLState() + "). Check your connection and data.");
            }
        }
    }

    private void printMenu() {
        output.println("""

                ========================================
                       CLASSROOM RENTAL APPLICATION
                ========================================
                CUSTOMERS
                 1. Register customer
                 2. Register booking user

                CLASSROOMS
                 3. List classrooms
                 4. Update classroom
                 5. Search available classrooms

                BOOKINGS
                 6. Create booking
                 7. View bookings by classroom
                 8. View bookings by customer
                 9. View upcoming bookings
                10. Cancel booking

                 0. Exit
                ----------------------------------------""");
    }

    private void registerCustomer() throws SQLException {
        CustomerType type = switch (read("Customer type (1 = individual, 2 = company): ")) {
            case "1" -> CustomerType.INDIVIDUAL;
            case "2" -> CustomerType.COMPANY;
            default -> throw new IllegalArgumentException("Choose customer type 1 or 2.");
        };
        String name = read("Customer name: ");
        String email = read("Email: ");
        long id = customers.registerCustomer(type, name, email);
        output.println("Customer registered. ID: " + id);
    }

    private void registerBookingUser() throws SQLException {
        long customerId = selectCustomer();
        String name = read("Booking user name: ");
        String email = read("Email: ");
        long id = customers.registerBookingUser(customerId, name, email);
        output.println("Booking user registered. ID: " + id);
    }

    private void updateClassroom() throws SQLException {
        printClassrooms(classrooms.listClassrooms());
        long id = readId("Classroom ID: ");
        Classroom current = classrooms.requireClassroom(id);
        output.println("Editing: " + current.getName());
        String name = read("New classroom name: ");
        int capacity = readCapacity("New capacity: ");
        boolean accessible = readYesNo("Accessible (y/n): ");
        Set<Equipment> equipment = readEquipment();
        classrooms.updateClassroom(id, name, capacity, accessible, equipment);
        output.println("Classroom updated.");
    }

    private void searchAvailable() throws SQLException {
        SearchCriteria criteria = readSearchCriteria();
        printClassrooms(findAvailable(criteria));
    }

    private void createBooking() throws SQLException {
        SearchCriteria criteria = readSearchCriteria();
        List<Classroom> available = findAvailable(criteria);
        printClassrooms(available);
        if (available.isEmpty()) {
            return;
        }
        long roomId = readId("Select classroom ID: ");
        if (available.stream().noneMatch(room -> room.getId() == roomId)) {
            throw new IllegalArgumentException("Select a classroom from the available results.");
        }

        long customerId = selectCustomer();
        List<BookingUser> users = customers.listBookingUsers(customerId);
        if (users.isEmpty()) {
            output.println("No booking users. Register one using option 2 first.");
            return;
        }
        output.println("Booking users:");
        for (BookingUser user : users) {
            output.printf("ID %d | %s | %s%n", user.getId(), user.getName(), user.getEmail());
        }
        long userId = readId("Booking user ID: ");
        if (users.stream().noneMatch(user -> user.getId() == userId)) {
            throw new IllegalArgumentException("Select a booking user for this customer.");
        }
        String comments = read("Comments (optional): ");
        Booking booking = new Booking(0, roomId, customerId, userId,
                criteria.start(), criteria.end(), comments);
        long id = bookings.createBooking(booking, criteria.capacity(), criteria.equipment(),
                criteria.accessible());
        output.println("Booking created. ID: " + id);
    }

    private void viewByClassroom() throws SQLException {
        printClassrooms(classrooms.listClassrooms());
        printBookings(bookings.listByClassroom(readId("Classroom ID: ")));
    }

    private void viewByCustomer() throws SQLException {
        printBookings(bookings.listByCustomer(selectCustomer()));
    }

    private void cancelBooking() throws SQLException {
        printBookings(bookings.listUpcoming());
        long id = readId("Booking ID to cancel: ");
        if (readYesNo("Confirm cancellation (y/n): ")) {
            bookings.cancelBooking(id);
            output.println("Booking cancelled.");
        } else {
            output.println("Cancellation abandoned.");
        }
    }

    private long selectCustomer() throws SQLException {
        List<Customer> registered = customers.listCustomers();
        if (registered.isEmpty()) {
            throw new IllegalArgumentException("No customers. Register a customer first.");
        }
        output.println("Customers:");
        for (Customer customer : registered) {
            output.printf("ID %d | %s | %s | %s%n", customer.getId(), customer.getName(),
                    customer.getType(), customer.getEmail());
        }
        long id = readId("Customer ID: ");
        customers.requireCustomer(id);
        return id;
    }

    private SearchCriteria readSearchCriteria() {
        LocalDateTime start = readDateTime("Start (yyyy-MM-dd HH:mm): ");
        LocalDateTime end = readDateTime("End   (yyyy-MM-dd HH:mm): ");
        int capacity = readCapacity("Minimum seating capacity: ");
        Set<Equipment> equipment = readEquipment();
        boolean accessible = readYesNo("Accessibility required (y/n): ");
        return new SearchCriteria(start, end, capacity, equipment, accessible);
    }

    private List<Classroom> findAvailable(SearchCriteria criteria) throws SQLException {
        return classrooms.searchAvailable(criteria.start(), criteria.end(), criteria.capacity(),
                criteria.equipment(), criteria.accessible());
    }

    private void printClassrooms(List<Classroom> rooms) {
        if (rooms.isEmpty()) {
            output.println("No matching classrooms found.");
            return;
        }
        output.println("Classrooms:");
        for (Classroom room : rooms) {
            output.printf("ID %d | %s | Capacity: %d | Accessible: %s | Equipment: %s%n",
                    room.getId(), room.getName(), room.getCapacity(),
                    room.isAccessible() ? "Yes" : "No", room.getEquipment());
        }
        output.println("Total classrooms: " + rooms.size());
    }

    private void printBookings(List<Booking> results) {
        if (results.isEmpty()) {
            output.println("No bookings found.");
            return;
        }
        output.println("Bookings:");
        for (Booking booking : results) {
            output.printf("ID %d | Room ID: %d | Customer ID: %d | Booking user ID: %d%n",
                    booking.getId(), booking.getClassroomId(), booking.getCustomerId(),
                    booking.getBookingUserId());
            output.printf("  %s -> %s%n", booking.getStartTime().format(DATE_TIME),
                    booking.getEndTime().format(DATE_TIME));
            if (booking.getComments() != null) {
                output.println("  Comments: " + booking.getComments());
            }
        }
        output.println("Total bookings: " + results.size());
    }

    private String read(String prompt) {
        output.print(prompt);
        output.flush();
        return input.nextLine().strip();
    }

    private long readId(String prompt) {
        try {
            long id = Long.parseLong(read(prompt));
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // Report the same error for invalid or out-of-range input.
        }
        throw new IllegalArgumentException("Enter a positive numeric ID.");
    }

    private int readCapacity(String prompt) {
        try {
            int capacity = Integer.parseInt(read(prompt));
            if (capacity > 0) {
                return capacity;
            }
        } catch (NumberFormatException ignored) {
            // Report the same error for invalid or out-of-range input.
        }
        throw new IllegalArgumentException("Enter a positive whole-number capacity.");
    }

    private LocalDateTime readDateTime(String prompt) {
        try {
            return LocalDateTime.parse(read(prompt), DATE_TIME);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Enter a valid date and time in yyyy-MM-dd HH:mm format.");
        }
    }

    private boolean readYesNo(String prompt) {
        return switch (read(prompt).toLowerCase(java.util.Locale.ROOT)) {
            case "y", "yes" -> true;
            case "n", "no" -> false;
            default -> throw new IllegalArgumentException("Enter y or n.");
        };
    }

    private Set<Equipment> readEquipment() {
        output.println("Equipment: 1 = PROJECTOR, 2 = WHITEBOARD, 3 = COMPUTERS, 4 = VIDEO_CONFERENCE");
        String selection = read("Equipment numbers separated by commas (blank = none): ");
        Set<Equipment> equipment = EnumSet.noneOf(Equipment.class);
        if (selection.isEmpty()) {
            return equipment;
        }
        for (String number : selection.split(",", -1)) {
            Equipment item = switch (number.strip()) {
                case "1" -> Equipment.PROJECTOR;
                case "2" -> Equipment.WHITEBOARD;
                case "3" -> Equipment.COMPUTERS;
                case "4" -> Equipment.VIDEO_CONFERENCE;
                default -> throw new IllegalArgumentException("Choose equipment numbers 1 to 4.");
            };
            equipment.add(item);
        }
        return equipment;
    }

    /** Groups the search values used by both availability and booking menus. */
    private record SearchCriteria(LocalDateTime start, LocalDateTime end, int capacity,
                                  Set<Equipment> equipment, boolean accessible) {
    }
}
