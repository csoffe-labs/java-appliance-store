package model;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for HomeAppliance domain behaviour.
 */
public class HomeApplianceTest {

    /**
     * createNew() should always produce a transient appliance
     * with ID = 0 prior to database persistence.
     */
    @Test
    public void createNew_setsIdToZero() {
        HomeAppliance h = HomeAppliance.createNew(
            "SKU1", "Bosch", "X100", "Washer", "Laundry", 19999, 10
        );
        assertEquals(0, h.getId());
    }

    /**
     * reduceStock() must return a new immutable object
     * and leave the original stock unchanged.
     */
    @Test
    public void reduceStock_returnsNewObject() {
        HomeAppliance h = new HomeAppliance(1, "A", "B", "C", "", "Cat", 100, 10);
        HomeAppliance reduced = h.reduceStock(3);

        assertEquals(10, h.getStockLevel());
        assertEquals(7, reduced.getStockLevel());
    }

    /**
     * reduceStock() should reject stock levels that
     * cannot satisfy the requested reduction.
     */
    @Test
    public void reduceStock_throwsIfInsufficientStock() {
        HomeAppliance h = new HomeAppliance(1, "A", "B", "C", "", "Cat", 100, 2);

        assertThrows(IllegalArgumentException.class, () ->
            h.reduceStock(5)
        );
    }
}