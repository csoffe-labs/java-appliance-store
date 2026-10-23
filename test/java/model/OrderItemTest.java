package model;

import static org.junit.Assert.*;
import org.junit.Test;
import java.time.LocalDate;

public class OrderItemTest {

    @Test
    public void getLineTotal_ShouldBeQuantityTimesUnitPrice() {
        OrderItem oi = new OrderItem(1, 1, 5, 2, 1000);
        assertEquals(5000, oi.getLineTotal());
    }

    @Test
    public void warrantyExpiryAddsCorrectYears() {
        OrderItem item = new OrderItem(0, 1, 1, 1, 2, 1000);

        LocalDate orderDate = LocalDate.of(2024, 1, 1);
        LocalDate expected = LocalDate.of(2026, 1, 1);

        assertEquals(expected, item.calculateWarrantyExpiry(orderDate));
    }

    
}