package classroomrental.model;

/** A company renting a classroom. */
public class CompanyCustomer extends Customer {
    public CompanyCustomer(long id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public CustomerType getType() {
        return CustomerType.COMPANY;
    }
}
