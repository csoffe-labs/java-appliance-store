package model;

public final class Customer {
	
	/**
	 *immutable customer record used in orders and the admin UI.
	 * Holds a business name, contact number & address.
	 * The object cannot be changed after creation - DAO uses withId() when saving.
	 * @author Chloe Soffe
	 */
    private final int id;           // 0 = not persisted yet
    private final String businessName;
    private final String telephone;  // can be empty
    private final Address address;

    public Customer(int id, String businessName, String telephone, Address address) {
        if (id < 0) throw new IllegalArgumentException("ID cannot be negative");
        if (businessName == null || businessName.trim().isEmpty()) 
            throw new IllegalArgumentException("Business name required");
        if (address == null) throw new IllegalArgumentException("Address required");
        
        this.id = id;
        this.businessName = businessName.trim();
        this.telephone = telephone != null ? telephone.trim() : "";
        this.address = address;
    }

    public Customer withId(int newId) {
        return new Customer(newId, businessName, telephone, address);
    }

    public int getId() { return id; }
    public String getBusinessName() { return businessName; }
    public String getTelephone() { return telephone; }
    public Address getAddress() { return address; }

    
    @Override
    public String toString() {
        return String.format(
            "| %-3d | %-20s | %-12s | %-50s |",
            id,
            businessName,
            telephone,
            address.asSingleLine()
        );
    }
}