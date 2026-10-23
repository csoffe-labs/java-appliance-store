package model;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for basket line-item behaviour (quantity and totals).
 */
public class ApplianceItemTest {

    /**
     * Increasing quantity should accumulate rather than replace.
     */
    @Test
    public void increaseQuantity_addsToExisting() {
        HomeAppliance p = new HomeAppliance(
            1, "SKU", "Brand", "Model", "Desc", "Cat", 1000, 10
        );

        ApplianceItem item = new ApplianceItem(p, 1, 1);
        item.increaseQuantity(3);

        assertEquals(4, item.getQuantity());
    }

    /**
     * Line total must equal quantity × unit price (in pence).
     */
    @Test
    public void lineTotal_isComputedCorrectly() {
        HomeAppliance p = new HomeAppliance(
            1, "SKU", "Brand", "Model", "Desc", "Cat", 500, 10
        );

        ApplianceItem item = new ApplianceItem(p, 4, 1);

        assertEquals(2000, item.getLineTotal());
    }
}