package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Holds the items a customer adds during a session.
 * Behavioural aggregate for creating orders.
 *
 * Merges lines when SAME product + SAME warranty.
 *
 * @author Chloe Soffe
 */
public class Basket {

    private final List<ApplianceItem> items = new ArrayList<>();

    
    public List<ApplianceItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    /**
     * Add item to basket
     * If same product + same warranty already exists > merge quantities
     */
    public void addItem(HomeAppliance product, int qty, int warrantyYears) {
        if (product == null)
            throw new IllegalArgumentException("Product required");
        if (qty <= 0)
            throw new IllegalArgumentException("Quantity must be positive");

        for (ApplianceItem item : items) {
            boolean sameProduct = item.getProduct().getId() == product.getId();
            boolean sameWarranty = item.getWarrantyYears() == warrantyYears;

            if (sameProduct && sameWarranty) {
                item.increaseQuantity(qty);
                return;
            }
        }

        items.add(new ApplianceItem(product, qty, warrantyYears));
    }

 
    public int getTotal() {
        int total = 0;
        for (ApplianceItem item : items) {
            total += item.getLineTotal();
        }
        return total;
    }

    /** Total number of units across all lines */
    public int getTotalItemCount() {
        int count = 0;
        for (ApplianceItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

  
    public void clear() {
        items.clear();
    }

    /** Sorting based on total price of line */
    public void sortByPrice() {
        items.sort(Comparator.comparingInt(ApplianceItem::getLineTotal));
    }

    /** Sorting highest warranty first */
    public void sortByWarranty() {
        items.sort(Comparator.comparingInt(ApplianceItem::getWarrantyYears).reversed());
    }
}