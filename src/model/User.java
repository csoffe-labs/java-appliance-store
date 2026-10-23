package model;
/**
 * Represents an authenticated user of the web app
 * Holds login details 
 * (e.g admin). Used by StoreServer to control 
 * restricted actions. Edit/Delete/Add for customers and products is for auth users
 *
 * @author Chloe Soffe
 */
public final class User {

    private final int id;         
    private final String username;
    private final String password;

    public User(int id, String username, String password) {

        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("Username required");

        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException("Password required");

        this.id = id < 0 ? 0 : id;
        this.username = username.trim();
        this.password = password.trim();
    }

    /** New instance with generated ID (DAO use only). */
    public User withId(int newId) {
        return new User(newId, username, password);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}