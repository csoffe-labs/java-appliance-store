package model;

import java.time.LocalDate;

/**
 * Represents a single line item in a completed order.
 * Immutable record - once created the values never change
 *
 * Used by Basket > to convert selected items into order items
 * OrderDAO > to persist historical purchase data
 *
 * @author Chloe Soffe
 */
public final class OrderItem {

    private final int id;           // 0 = not persisted yet
    private final int orderId;         // foreign key (0 = not assigned yet)
    private final int homeApplianceId;   // foreign key
    private final int quantity;
    private final int warrantyYears;      
    private final int priceAtPurchase;    
    
    /**
     * Full constructor used when loading from the database
     * OR creating fully populated items in OrderDAO.
     */
    public OrderItem(int id, int orderId, int homeApplianceId,
                     int quantity, int warrantyYears, int priceAtPurchase) {

        if (id < 0) throw new IllegalArgumentException("ID cannot be negative");
        if (orderId < 0) throw new IllegalArgumentException("Order ID cannot be negative");
        if (homeApplianceId <= 0) throw new IllegalArgumentException("Appliance ID must be positive");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be at least 1");
        if (warrantyYears < 0) throw new IllegalArgumentException("Warranty years cannot be negative");
        if (priceAtPurchase < 0) throw new IllegalArgumentException("Price cannot be negative");

        this.id = id;
        this.orderId = orderId;
        this.homeApplianceId = homeApplianceId;
        this.quantity = quantity;
        this.warrantyYears = warrantyYears;
        this.priceAtPurchase = priceAtPurchase;
    }

    /**
     * Constructor new items Basket > Order.
     * Order ID is unknown until the Order is saved.
     */
    public OrderItem(int orderId, int homeApplianceId,
                     int quantity, int warrantyYears, int priceAtPurchase) {

        this(0, orderId, homeApplianceId, quantity, warrantyYears, priceAtPurchase);
    }

    
    /**
     * Returns new instance with the auto-gen database ID
     * Used by OrderItemDAO after insert
     */
    public OrderItem withId(int newId) {
        if (newId <= 0)
            throw new IllegalArgumentException("New ID must be positive");

        return new OrderItem(
            newId,
            orderId,
            homeApplianceId,
            quantity,
            warrantyYears,
            priceAtPurchase
        );
    }

    /**
     * Returns a new instance with the final orderId
     * after OrderDAO inserts the parent order.
     */
    public OrderItem withOrderId(int newOrderId) {
        if (newOrderId <= 0)
            throw new IllegalArgumentException("Order ID must be positive");

        return new OrderItem(
            id,
            newOrderId,
            homeApplianceId,
            quantity,
            warrantyYears,
            priceAtPurchase
        );
    }

    

    public int getLineTotal() {
        return quantity * priceAtPurchase;
    }

    public double getLineTotalPounds() {
        return getLineTotal() / 100.0;
    }

    public double getUnitPricePounds() {
        return priceAtPurchase / 100.0;
    }

    public boolean hasWarranty() {
        return warrantyYears > 0;
    }

    public LocalDate calculateWarrantyExpiry(LocalDate orderDate) {
        if (orderDate == null)
            throw new IllegalArgumentException("Order date cannot be null");

        return orderDate.plusYears(warrantyYears);
    }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getHomeApplianceId() { return homeApplianceId; }
    public int getQuantity() { return quantity; }
    public int getWarrantyYears() { return warrantyYears; }
    public int getPriceAtPurchase() { return priceAtPurchase; }

    @Override
    public String toString() {
        return String.format(
            "OrderItem[id=%d, order=%d, appliance=%d, qty=%d, warranty=%dyr, £%.2f]",
            id, orderId, homeApplianceId, quantity, warrantyYears, getUnitPricePounds()
        );
    }
}
