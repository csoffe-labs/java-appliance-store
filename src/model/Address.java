package model;

/**
 * Immutable value object representing a postal address for a customer.
 * Performs basic validation and trims all input.
 *
 * @author Chloe Soffe
 */
public final class Address {

    private final String line1;
    private final String line2;   // optional
    private final String town;
    private final String country;
    private final String postCode;

    public Address(String line1, String line2, String town, String country, String postCode) {

   // safety with null
        if (line1 == null || town == null || country == null || postCode == null) {
            throw new IllegalArgumentException("Address fields cannot be null");
        }

  // Basic validation
        if (line1.trim().length() > 120) {
            throw new IllegalArgumentException("First line of address cannot exceed 120 characters");
        }
        if (town.trim().length() > 80) {
            throw new IllegalArgumentException("Town cannot exceed 80 characters");
        }
        if (country.trim().length() > 50) {
            throw new IllegalArgumentException("Country cannot exceed 50 characters");
        }
        if (postCode.trim().length() > 10) {
            throw new IllegalArgumentException("Postcode cannot exceed 10 characters");
        }

        this.line1 = line1.trim();
        this.line2 = (line2 == null) ? "" : line2.trim();
        this.town = town.trim();
        this.country = country.trim();
        this.postCode = postCode.trim();
    }

    
    public String getLine1() { return line1; }
    public String getLine2() { return line2; }
    public String getTown() { return town; }
    public String getCountry() { return country; }
    public String getPostCode() { return postCode; }

   
    public String asSingleLine() {
        return line2.isEmpty()
                ? line1 + ", " + town + ", " + country + ", " + postCode
                : line1 + ", " + line2 + ", " + town + ", " + country + ", " + postCode;
    }

    @Override
    public String toString() {
        return asSingleLine();
    }
}