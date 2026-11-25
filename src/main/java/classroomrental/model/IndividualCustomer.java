package classroomrental.model;

/** A private individual renting a classroom. */
public class IndividualCustomer extends Customer {
    public IndividualCustomer(long id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public CustomerType getType() {
        return CustomerType.INDIVIDUAL;
    }
}
