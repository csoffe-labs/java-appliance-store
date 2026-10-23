package dao;

import model.Order;
import model.OrderItem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for creating and loading Orders.
 * 
 * A complete order (Order + OrderItems + stock reduction)
 * is created atomically using a single JDBC transaction.
 *
 * This DAO follows the same structure as HomeApplianceDAO
 * and CustomerDAO 
 *
 * @author Chloe Soffe
 */
public class OrderDAO {

    private final String dbUrl;

    public OrderDAO(String dbUrl) {
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
     * Creates an Order with all its items in a single transaction.
     * Reduces stock levels after inserting items.
     *
     * @param order  The Order header (id = 0 for new orders)
     * @param items  List of OrderItems (orderId ignored)
     * @return Saved Order with generated ID
     */
    public Order createCompleteOrder(Order order, List<OrderItem> items) {

        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);   // begin transaction

   
            // 1 Insert order header
            String orderSql =
                "INSERT INTO orders (customerId, orderDate, total) VALUES (?, ?, ?)";

            int orderId;

            try (PreparedStatement ps =
                     conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, order.getCustomerId());
                ps.setString(2, order.getOrderDate().toString());
                ps.setInt(3, order.getTotal());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next())
                        throw new SQLException("Failed to retrieve order ID");
                    orderId = keys.getInt(1);
                }
            }

			// 2 insert items using generated ID
				String itemSql =
                "INSERT INTO order_item " +
                "(orderId, homeApplianceId, quantity, warrantyYears, priceAtPurchase) " +
                "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {

                for (OrderItem item : items) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getHomeApplianceId());
                    ps.setInt(3, item.getQuantity());
                    ps.setInt(4, item.getWarrantyYears());
                    ps.setInt(5, item.getPriceAtPurchase());
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            
            // 3 Reduce stock levels (validation included)
				String stockSql =
                "UPDATE homeappliance SET stockLevel = stockLevel - ? " +
                "WHERE id = ? AND stockLevel >= ?";

            try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                for (OrderItem item : items) {
                    ps.setInt(1, item.getQuantity());
                    ps.setInt(2, item.getHomeApplianceId());
                    ps.setInt(3, item.getQuantity());

                    // If this fails, stock is insufficient → rollback
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                            "Insufficient stock for product ID " + item.getHomeApplianceId()
                        );
                    }
                }
            }

            conn.commit();

            // Return correct immutable Order instance
            return new Order(orderId,
                             order.getCustomerId(),
                             order.getOrderDate(),
                             order.getTotal());

        } catch (Exception ex) {

            // roll back safely on ANY failure
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }

            throw new RuntimeException("Failed to create order: " + ex.getMessage(), ex);

        } finally {

            // restore autocommit + close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }


    // ============================================================
    // Simple lookup methods (unchanged)
    // ============================================================

    public Optional<Order> findById(int id) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Order(
                        rs.getInt("id"),
                        rs.getInt("customerId"),
                        LocalDate.parse(rs.getString("orderDate")),
                        rs.getInt("total")
                    ));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order " + id, e);
        }
    }

    public List<Order> findByCustomerId(int customerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customerId = ? ORDER BY orderDate DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("customerId"),
                        LocalDate.parse(rs.getString("orderDate")),
                        rs.getInt("total")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find orders", e);
        }

        return list;
    }
}