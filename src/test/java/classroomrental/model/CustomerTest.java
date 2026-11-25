package classroomrental.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {
    @Test
    void customerSubclassesReturnTheirOwnTypes() {
        List<Customer> customers = List.of(
                new IndividualCustomer(1, "Alex Example", "alex@example.com"),
                new CompanyCustomer(2, "Example AB", "office@example.org"));
        assertEquals(List.of(CustomerType.INDIVIDUAL, CustomerType.COMPANY),
                customers.stream().map(Customer::getType).toList());
    }

    @Test
    void storesCommonCustomerDetails() {
        Customer customer = new CompanyCustomer(2, " Example AB ", " office@example.org ");
        assertEquals(2, customer.getId());
        assertEquals("Example AB", customer.getName());
        assertEquals("office@example.org", customer.getEmail());
    }

    @Test
    void rejectsInvalidCustomerDetails() {
        assertThrows(IllegalArgumentException.class,
                () -> new IndividualCustomer(-1, "Alex", "alex@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> new IndividualCustomer(0, " ", "alex@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> new CompanyCustomer(0, "Example AB", null));
    }

    @Test
    void bookingUserBelongsToCustomer() {
        BookingUser user = new BookingUser(1, 2, "Sam Example", "sam@example.org");
        assertEquals(2, user.getCustomerId());
        assertEquals("Sam Example", user.getName());
        assertThrows(IllegalArgumentException.class,
                () -> new BookingUser(0, 0, "Sam", "sam@example.org"));
    }
}
