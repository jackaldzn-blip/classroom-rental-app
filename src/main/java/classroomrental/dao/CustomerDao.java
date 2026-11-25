package classroomrental.dao;

import classroomrental.model.*;
import classroomrental.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persists customers and converts database rows to the correct customer subclass. */
public class CustomerDao {
    private final DatabaseConnection database;

    public CustomerDao(DatabaseConnection database) {
        this.database = database;
    }

    public long create(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, email, customer_type) VALUES (?, ?, ?)";
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getType().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("Customer ID was not generated.");
            }
        }
    }

    public Optional<Customer> findById(long id) throws SQLException {
        String sql = "SELECT id, name, email, customer_type FROM customers WHERE id = ?";
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT id, name, email, customer_type FROM customers ORDER BY name, id";
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = database.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                customers.add(map(result));
            }
        }
        return customers;
    }

    private Customer map(ResultSet result) throws SQLException {
        long id = result.getLong("id");
        String name = result.getString("name");
        String email = result.getString("email");
        return switch (CustomerType.valueOf(result.getString("customer_type"))) {
            case INDIVIDUAL -> new IndividualCustomer(id, name, email);
            case COMPANY -> new CompanyCustomer(id, name, email);
        };
    }
}
