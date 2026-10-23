package model;

import java.time.LocalDate;

/**
 * Represents a customer order. Immutable value object.
 * Stores the customer, date, & total cost (in pence).
 * Individual product lines are represented by OrderItem.
 * Acts as the root aggregate for persistence.
 *
 * @author Chloe Soffe
 */
public final class Order {

    private final int id;          // 0 = not persisted yet
    private final int customerId;     
    private final LocalDate orderDate;
    private final int total;          

    public Order(int id, int customerId, LocalDate orderDate, int total) {
        if (id < 0) throw new IllegalArgumentException("ID cannot be negative");
        if (customerId <= 0) throw new IllegalArgumentException("Customer ID must be positive");
        if (orderDate == null) throw new IllegalArgumentException("Order date cannot be null");
        if (total < 0) throw new IllegalArgumentException("Total cannot be negative");

        this.id = id;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.total = total;
    }

    /** Factory for creating new (unsaved) & orders. */
    public static Order createNew(int customerId, LocalDate orderDate, int total) {
        return new Order(0, customerId, orderDate, total);
    }

    /** Returns a copy with the generated ID. */
    public Order withId(int newId) {
        return new Order(newId, customerId, orderDate, total);
    }

    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public LocalDate getOrderDate() { return orderDate; }
    public int getTotal() { return total; }

    public double getTotalAsPounds() {
        return total / 100.0;
    }

    @Override
    public String toString() {
        return String.format(
            "Order #%d | Customer %d | %s | £%.2f",
            id, customerId, orderDate, getTotalAsPounds()
        );
    }
}