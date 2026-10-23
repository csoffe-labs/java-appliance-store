package dao;

import model.Customer;
import model.Address;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for Customer entities.
 * Provides full CRUD and reconstructs immutable Customer + Address
 * objects from the database.
 *
 * @author Chloe Soffe
 */
public class CustomerDAO {

    private final String dbUrl;

    public CustomerDAO(String dbUrl) {
        this.dbUrl = dbUrl;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

   
    private Customer mapRow(ResultSet rs) throws SQLException {
        Address address = new Address(
                rs.getString("addressLine1"),
                rs.getString("addressLine2"),
                rs.getString("town"),
                rs.getString("country"),
                rs.getString("postCode")
        );

        return new Customer(
                rs.getInt("id"),
                rs.getString("businessName"),
                rs.getString("telephone"),
                address
        );
    }

    
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customer ORDER BY businessName";
        List<Customer> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load customers", e);
        }

        return results;
    }

    /** Find one Customer ID - uses Optional */
    public Optional<Customer> findById(int id) {
        String sql = "SELECT * FROM customer WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load customer " + id, e);
        }
    }

    /** 
     * Insert immutable Customer, return new instance with generated ID 
     */
    public Customer insert(Customer c) {
        String sql =
            "INSERT INTO customer (businessName, telephone, addressLine1, addressLine2, town, country, postCode) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getBusinessName());
            ps.setString(2, c.getTelephone());
            ps.setString(3, c.getAddress().getLine1());
            ps.setString(4, c.getAddress().getLine2());
            ps.setString(5, c.getAddress().getTown());
            ps.setString(6, c.getAddress().getCountry());
            ps.setString(7, c.getAddress().getPostCode());

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("Insert failed");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    return c.withId(newId);
                }
                throw new RuntimeException("Failed to retrieve customer ID");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert customer", e);
        }
    }

        
    

    /** Update an existing immutable customer */
    public boolean update(Customer c) {
        String sql =
                "UPDATE customer SET businessName=?, telephone=?, addressLine1=?, addressLine2=?, town=?, country=?, postCode=? " +
                "WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getBusinessName());
            ps.setString(2, c.getTelephone());
            ps.setString(3, c.getAddress().getLine1());
            ps.setString(4, c.getAddress().getLine2());
            ps.setString(5, c.getAddress().getTown());
            ps.setString(6, c.getAddress().getCountry());
            ps.setString(7, c.getAddress().getPostCode());
            ps.setInt(8, c.getId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update customer " + c.getId(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM customer WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete customer " + id, e);
        }
    }
}