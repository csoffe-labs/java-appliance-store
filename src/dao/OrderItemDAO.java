package dao;

import model.OrderItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for accessing OrderItem records
 * 
 * Creation of new order items is handled through
 * OrderDAO.createCompleteOrder() to keep checkout atomic
 * This DAO is used for reading order items and small admin level queries
 * @author Chloe Soffe
 */
public class OrderItemDAO {
    
    private final String dbUrl;

    public OrderItemDAO(String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver missing", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    /**
     * 
     * Creates immutable OrderItem with ID included
     */
    private OrderItem mapRow(ResultSet rs) throws SQLException {
        return new OrderItem(
            rs.getInt("id"),           // Include ID in constructor
            rs.getInt("orderId"),
            rs.getInt("homeApplianceId"),
            rs.getInt("quantity"),
            rs.getInt("warrantyYears"),
            rs.getInt("priceAtPurchase")
        );
    }

    
    public List<OrderItem> findByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_item WHERE orderId = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load items for order " + orderId, e);
        }

        return items;
    }

    //Optional findbyID
    public Optional<OrderItem> findById(int id) {
        String sql = "SELECT * FROM order_item WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order item " + id, e);
        }
    }

    

    
//remove extended warrant

    /**
     * Calculate total from a specific product.
     * Returns total in pence
     */
    public int getProductRevenue(int homeApplianceId) {
        String sql = "SELECT SUM(quantity * priceAtPurchase) as revenue " +
                     "FROM order_item WHERE homeApplianceId = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, homeApplianceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("revenue");
                }
            }
            
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate revenue for product " + homeApplianceId, e);
        }
    }

    
    public boolean updateWarranty(int orderItemId, int newWarrantyYears) {
        String sql = "UPDATE order_item SET warrantyYears = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newWarrantyYears);
            ps.setInt(2, orderItemId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update warranty for item " + orderItemId, e);
        }
    }

    
}