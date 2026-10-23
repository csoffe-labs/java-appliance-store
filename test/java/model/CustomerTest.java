package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Customer immutability and validation behaviour.
 */
public class CustomerTest {

    @Test
    public void creatingCustomerWithoutBusinessNameThrows() {
        Address addr = new Address("L1", "", "Town", "UK", "AA1");

        assertThrows(IllegalArgumentException.class, () ->
            new Customer(0, "", "0123", addr)
        );
    }

    @Test
    public void creatingCustomerWithoutAddressThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Customer(0, "Biz", "0123", null)
        );
    }

    @Test
    public void withIdCreatesNewInstanceAndDoesNotMutateOriginal() {
        Address addr = new Address("L1", "", "Town", "UK", "AA1");
        Customer original = new Customer(0, "Biz", "0123", addr);

        Customer updated = original.withId(99);

        assertEquals(0, original.getId(), "Original customer must remain unchanged");
        assertEquals(99, updated.getId(), "New customer must have the new ID");
        assertNotSame(original, updated, "withId() must return a different instance");
    }

    @Test
    public void addressCompositionFormatsCorrectly() {
        Address addr = new Address("Line1", "Line2", "Town", "UK", "AA1");
        Customer c = new Customer(0, "Biz", "0123", addr);

        assertEquals("Line1, Line2, Town, UK, AA1", c.getAddress().asSingleLine());
    }
}