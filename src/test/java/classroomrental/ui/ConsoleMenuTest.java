package classroomrental.ui;

import classroomrental.model.CustomerType;
import classroomrental.service.CustomerService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleMenuTest {
    private static final String DATES = "2030-01-15 10:00\n2030-01-15 12:00\n";

    // These tests exercise input and menu navigation without opening a database.
    private String runMenu(String commands) {
        return runMenu(commands, null);
    }

    private String runMenu(String commands, CustomerService customers) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(commands);
             PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            new ConsoleMenu(customers, null, null, input, output).run();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void displaysMenuAndExits() {
        String output = runMenu("0\n");
        assertTrue(output.contains("CLASSROOM RENTAL APPLICATION"));
        assertTrue(output.contains("Create booking"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void exitsWhenInputEnds() {
        assertTrue(runMenu("").contains("Input closed. Goodbye!"));
    }

    @Test
    void rejectsInvalidMenuOptionAndContinues() {
        String output = runMenu("99\n0\n");
        assertTrue(output.contains("Invalid menu option"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void rejectsInvalidCustomerType() {
        String output = runMenu("1\n3\n0\n");
        assertTrue(output.contains("Choose customer type 1 or 2."));
    }

    @Test
    void rejectsInvalidCalendarDate() {
        String output = runMenu("5\n2030-02-30 10:00\n0\n");
        assertTrue(output.contains("Enter a valid date and time"));
    }

    @Test
    void rejectsInvalidCapacity() {
        String output = runMenu("5\n" + DATES + "0\n0\n");
        assertTrue(output.contains("Enter a positive whole-number capacity."));
    }

    @Test
    void rejectsUnknownEquipment() {
        String output = runMenu("5\n" + DATES + "20\n9\n0\n");
        assertTrue(output.contains("Choose equipment numbers 1 to 4."));
    }

    @Test
    void rejectsInvalidYesNoInput() {
        String output = runMenu("5\n" + DATES + "20\n1,2\nmaybe\n0\n");
        assertTrue(output.contains("Enter y or n."));
    }

    @Test
    void exitsSafelyDuringIncompleteInput() {
        assertTrue(runMenu("1\n2\n").contains("Input closed. Goodbye!"));
    }

    @Test
    void passesCustomerDetailsToServiceAndDisplaysGeneratedId() {
        CustomerService service = new CustomerService(null, null) {
            @Override
            public long registerCustomer(CustomerType type, String name, String email) {
                assertEquals(CustomerType.COMPANY, type);
                assertEquals("Example AB", name);
                assertEquals("office@example.org", email);
                return 42;
            }
        };
        String output = runMenu("1\n2\nExample AB\noffice@example.org\n0\n", service);
        assertTrue(output.contains("Customer registered. ID: 42"));
    }
}
