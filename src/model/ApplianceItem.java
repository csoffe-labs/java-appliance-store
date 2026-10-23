package model;

/**
 * 
 * Wraps a HomeAppliance with basket specific details - quantity and chosen warranty
 * lightweight behavioural object used for line totals, sorting & conversion to OrderItem.
 *
 * Temporary object, at checkout it becomes an OrderItem for storage
 * @author Chloe Soffe
 */
 
public final class ApplianceItem {

    private final HomeAppliance product;
    private int quantity; //must merge is SKU & warranty are the same
    private final int warrantyYears;

    public ApplianceItem(HomeAppliance product, int quantity, int warrantyYears) {
        if (product == null)
            throw new IllegalArgumentException("A product is required");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        if (warrantyYears <= 0)
            throw new IllegalArgumentException("Warranty must be at least 1 year");

        this.product = product;
        this.quantity = quantity;
        this.warrantyYears = warrantyYears;
    }

    /**
     * Persistent OrderItem using the current product details
     * Price stored is a fixed snapshot at time of purchase purchase.
     */
    public OrderItem toOrderItem(int orderId) {
        return new OrderItem(
            orderId,
            product.getId(),
            quantity,
            warrantyYears,
            product.getPrice()  
            // snapshot of price at the time of purchase
        );
        
    }
    public void increaseQuantity(int extra) {
        if (extra < 1) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity += extra;
    }
   
    public int getLineTotal() {
        return quantity * product.getPrice();
    }

 
    public HomeAppliance getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public int getWarrantyYears() { return warrantyYears; }

}
	
