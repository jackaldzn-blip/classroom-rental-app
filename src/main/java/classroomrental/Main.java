package classroomrental;

import classroomrental.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Temporary startup check; the console menu will replace it in Phase 5. */
public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseConnection database = DatabaseConnection.fromEnvironment();
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM classrooms");
             ResultSet result = statement.executeQuery()) {
            result.next();
            System.out.println("Classroom Rental App");
            System.out.println("Connected to MySQL successfully.");
            System.out.println("Database: " + connection.getCatalog());
            System.out.println("Classrooms: " + result.getInt(1));
        }
    }
}
