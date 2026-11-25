package classroomrental;

import classroomrental.dao.BookingDao;
import classroomrental.dao.BookingUserDao;
import classroomrental.dao.ClassroomDao;
import classroomrental.dao.CustomerDao;
import classroomrental.service.BookingService;
import classroomrental.service.ClassroomService;
import classroomrental.service.CustomerService;
import classroomrental.ui.ConsoleMenu;
import classroomrental.util.DatabaseConnection;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/** Starts the application and connects the UI, service and DAO layers. */
public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseConnection database = DatabaseConnection.fromEnvironment();

        try {
            // Verify connectivity before displaying the menu.
            try (Connection connection = database.open()) {
                System.out.println("Connected to MySQL: " + connection.getCatalog());
            }

            CustomerDao customerDao = new CustomerDao(database);
            BookingUserDao bookingUserDao = new BookingUserDao(database);
            ClassroomDao classroomDao = new ClassroomDao(database);
            BookingDao bookingDao = new BookingDao(database);

            CustomerService customers = new CustomerService(customerDao, bookingUserDao);
            ClassroomService classrooms = new ClassroomService(classroomDao, bookingDao);
            BookingService bookings = new BookingService(bookingDao, bookingUserDao,
                    customers, classrooms);

            try (Scanner input = new Scanner(System.in)) {
                new ConsoleMenu(customers, classrooms, bookings, input, System.out).run();
            }
        } finally {
            // Stop Connector/J's background thread when the application exits.
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }
}
