package classroomrental.service;

import classroomrental.dao.BookingUserDao;
import classroomrental.dao.CustomerDao;
import classroomrental.model.*;
import java.sql.SQLException;
import java.util.List;

/** Coordinates customer registration and booking-user relationships. */
public class CustomerService {
    private final CustomerDao customers;
    private final BookingUserDao users;

    public CustomerService(CustomerDao customers, BookingUserDao users) {
        this.customers = customers;
        this.users = users;
    }

    public long registerCustomer(CustomerType type, String name, String email)
            throws SQLException {
        if (type == null) {
            throw new IllegalArgumentException("Choose a customer type.");
        }
        Customer customer = switch (type) {
            case INDIVIDUAL -> new IndividualCustomer(0, name, email);
            case COMPANY -> new CompanyCustomer(0, name, email);
        };
        return customers.create(customer);
    }

    public long registerBookingUser(long customerId, String name, String email)
            throws SQLException {
        requireCustomer(customerId);
        return users.create(new BookingUser(0, customerId, name, email));
    }

    public List<Customer> listCustomers() throws SQLException {
        return customers.findAll();
    }

    public List<BookingUser> listBookingUsers(long customerId) throws SQLException {
        requireCustomer(customerId);
        return users.findByCustomerId(customerId);
    }

    public Customer requireCustomer(long id) throws SQLException {
        return customers.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));
    }
}
