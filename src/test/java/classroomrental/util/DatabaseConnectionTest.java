package classroomrental.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {
    private static final String URL = "jdbc:mysql://localhost:3306/classroom_rental";

    @Test
    void acceptsValidConfiguration() {
        assertDoesNotThrow(() -> new DatabaseConnection(URL, "demo", "example"));
    }

    @Test
    void rejectsMissingUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseConnection(null, "demo", "example"));
    }

    @Test
    void rejectsNonMysqlUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseConnection("jdbc:sqlite:test.db", "demo", "example"));
    }

    @Test
    void rejectsBlankUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseConnection(URL, " ", "example"));
    }

    @Test
    void rejectsMissingPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseConnection(URL, "demo", null));
    }
}
