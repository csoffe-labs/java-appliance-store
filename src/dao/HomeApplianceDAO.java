package dao;

import model.HomeAppliance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles database access for HomeAppliance products.
 * Full CRUD operations & lightweight search support.
 * @author Chloe Soffe
 */

public class HomeApplianceDAO {

    private final String dbUrl;

    public HomeApplianceDAO(String dbUrl) {
        this.dbUrl = dbUrl;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite driver not found", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private HomeAppliance mapRow(ResultSet rs) throws SQLException {
        return new HomeAppliance(
                rs.getInt("id"),
                rs.getString("sku"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getString("description"),
                rs.getString("category"),
                rs.getInt("price"),
                rs.getInt("stockLevel")
        );
    }

    public List<HomeAppliance> findAll() {
        String sql = "SELECT * FROM homeappliance ORDER BY brand, model"; // ✔ correct table
        List<HomeAppliance> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load appliances", e);
        }

        return results;
    }

 //Optional use with find by ID
    public Optional<HomeAppliance> findById(int id) {
        String sql = "SELECT * FROM homeappliance WHERE id=?"; // ✔ corrected

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load appliance " + id, e);
        }
    }

    public HomeAppliance insert(HomeAppliance a) {
        String sql = "INSERT INTO homeappliance (sku, brand, model, description, category, price, stockLevel) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)"; // ✔ corrected

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getSku());
            ps.setString(2, a.getBrand());
            ps.setString(3, a.getModel());
            ps.setString(4, a.getDescription());
            ps.setString(5, a.getCategory());
            ps.setInt(6, a.getPrice());
            ps.setInt(7, a.getStockLevel());

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("Insert failed");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    return new HomeAppliance(
                            newId,
                            a.getSku(),
                            a.getBrand(),
                            a.getModel(),
                            a.getDescription(),
                            a.getCategory(),
                            a.getPrice(),
                            a.getStockLevel()
                    );
                }
            }

            throw new RuntimeException("Generated ID missing");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert appliance", e);
        }
    }

    public boolean update(HomeAppliance a) {
        String sql = "UPDATE homeappliance SET sku=?, brand=?, model=?, description=?, category=?, price=?, stockLevel=? WHERE id=?"; // ✔ corrected

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getSku());
            ps.setString(2, a.getBrand());
            ps.setString(3, a.getModel());
            ps.setString(4, a.getDescription());
            ps.setString(5, a.getCategory());
            ps.setInt(6, a.getPrice());
            ps.setInt(7, a.getStockLevel());
            ps.setInt(8, a.getId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update appliance " + a.getId(), e);
        }
    }

 
    public boolean delete(int id) {
        String sql = "DELETE FROM homeappliance WHERE id=?"; // ✔ corrected

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete appliance " + id, e);
        }
    }

    //stock reduce
    public boolean updateStock(int applianceId, int quantity) {
        String sql = "UPDATE homeappliance SET stockLevel = stockLevel - ? WHERE id = ? AND stockLevel >= ?"; // ✔ corrected

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, applianceId);
            ps.setInt(3, quantity);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update stock for appliance " + applianceId, e);
        }
    }

		/** search by item description implementned in UI via search bar */
    public List<HomeAppliance> searchByDescription(String q) {
    	if (q == null || q.trim().isEmpty()) {
            return findAll();   
        }

        String sql = "SELECT * FROM homeappliance "
                   + "WHERE LOWER(description) LIKE ? "
                   + "ORDER BY brand, model";

        List<HomeAppliance> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + q.toLowerCase().trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Search failed", e);
        }

        return results;
    }}