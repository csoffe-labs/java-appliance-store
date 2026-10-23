package model;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for Basket merging and total calculation behaviour.
 */
public class BasketTest {

    /**
     * SAME product + SAME warranty should merge into one line item.
     */
    @Test
    public void addingSameProductSameWarranty_mergesQuantities() {
        Basket b = new Basket();

        HomeAppliance p = new HomeAppliance(
            1, "SKU1", "Brand", "Model", "Desc", "Cat", 1000, 10
        );

        b.addItem(p, 1, 1);
        b.addItem(p, 2, 1);

        assertEquals(1, b.getItems().size());
        assertEquals(3, b.getItems().get(0).getQuantity());
    }

    /**
     * SAME product but DIFFERENT warranty must not merge.
     */
    @Test
    public void addingSameProductDifferentWarranty_createsSeparateLines() {
        Basket b = new Basket();

        HomeAppliance p = new HomeAppliance(
            1, "SKU1", "Brand", "Model", "Desc", "Cat", 1000, 10
        );

        b.addItem(p, 1, 1); // 1-year
        b.addItem(p, 1, 3); // 3-year

        assertEquals(2, b.getItems().size());
    }

    /**
     * Total cost should sum all line totals in pence.
     */
    @Test
    public void basketTotalSumsAllLineTotals() {
        Basket b = new Basket();

        HomeAppliance p1 = new HomeAppliance(1, "A", "B", "C", "D", "Cat", 1000, 10);
        HomeAppliance p2 = new HomeAppliance(2, "E", "F", "G", "H", "Cat", 500, 10);

        b.addItem(p1, 2, 1); // 2000 pence
        b.addItem(p2, 3, 1); // 1500 pence

        assertEquals(3500, b.getTotal());
    }
}