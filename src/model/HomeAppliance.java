package model;

/**
 * Represents a product in the store. Immutable value object used across
 * console and web layers. 
 * Provides small behaviour helpers - category normalisation, basic textstring for search
 * 
 * safe stock checks and reductions
 * price formatting for display
 *
 * @author Chloe Soffe
 */
public final class HomeAppliance {

    private final int id;            
    private final String sku;
    private final String brand;
    private final String model;
    private final String description;
    private final String category;
    private final int price;          
    private final int stockLevel;

    public HomeAppliance(int id,
                         String sku,
                         String brand,
                         String model,
                         String description,
                         String category,
                         int price,
                         int stockLevel) {

        if (sku == null || sku.trim().isEmpty())
            throw new IllegalArgumentException("SKU required");
        if (brand == null || brand.trim().isEmpty())
            throw new IllegalArgumentException("Brand required");
        if (model == null || model.trim().isEmpty())
            throw new IllegalArgumentException("Model required");
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        if (stockLevel < 0)
            throw new IllegalArgumentException("Stock cannot be negative");

        if (id < 0)
            throw new IllegalArgumentException("ID cannot be negative"); //fail-fast validation -refactor Math.max

        this.id = id;
        this.sku = sku.trim();
        this.brand = brand.trim();
        this.model = model.trim();
        this.description = description == null ? "" : description.trim();
        this.category = category == null ? "" : category.trim();
        this.price = price;
        this.stockLevel = stockLevel;
    }


    // Foctories
  

    public static HomeAppliance createNew(String sku, String brand, String model,
                                          String description, String category,
                                          int price, int stockLevel) {

        return new HomeAppliance(0, sku, brand, model, description, category, price, stockLevel);
    }

    public static HomeAppliance forUpdate(int id, String sku, String brand, String model,
                                          String description, String category,
                                          int price, int stockLevel) {

        return new HomeAppliance(id, sku, brand, model, description, category, price, stockLevel);
    }

    //domain behaviour 

    public String getCategoryNormalised() {
        return category.toLowerCase();
    }

    public String getCategoryPretty() {
        if (category.isEmpty()) return "";
        String c = category.toLowerCase();
        return Character.toUpperCase(c.charAt(0)) + c.substring(1);
    }

    public boolean isInCategory(String... options) {
        String key = getCategoryNormalised();
        for (String opt : options) {
            if (key.equals(opt.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

  

    public boolean inStock() {
        return stockLevel > 0;
    }

    public boolean canFulfill(int qty) {
        return qty > 0 && stockLevel >= qty;
    }

    /** Returns a *new* object with reduced stock. */
    
    public HomeAppliance reduceStock(int qty) {
        if (!canFulfill(qty)) {
            throw new IllegalArgumentException("Not enough stock");
        }
        return new HomeAppliance(
            id, sku, brand, model, description, category,
            price,
            stockLevel - qty
        );
    }

    //search function 

    
    public boolean matches(String q) {
        if (q == null || q.trim().isEmpty()) return true;

        String s = q.trim().toLowerCase();

        return sku.toLowerCase().contains(s)
            || brand.toLowerCase().contains(s)
            || model.toLowerCase().contains(s)
            || description.toLowerCase().contains(s)
            || category.toLowerCase().contains(s);
    }

    
    public double getPriceAsPounds() {
        return price / 100.0;
    }


    // immutable object
  

    public int getId() { return id; }
    public String getSku() { return sku; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public int getPrice() { return price; }
    public int getStockLevel() { return stockLevel; }

    @Override
    public String toString() {
        return String.format(
            "| %-3d | %-10s | %-12s | %-15s | %-12s | £%-8.2f | %-3d |",
            id,
            sku,
            brand,
            model,
            category,
            price / 100.0,
            stockLevel
        );
    }
}