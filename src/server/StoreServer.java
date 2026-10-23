package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import dao.CustomerDAO;
import dao.HomeApplianceDAO;
import dao.OrderDAO;
import dao.UserDAO;
import handlers.AdminHandler;
import handlers.BasketHandler;
import handlers.CustomerHandler;
import handlers.ProductHandler;
import model.Basket;

/**
 * Entry point for the web application.
 * No static state, DAOs & shared objects injected into handlers
 * @author Chloe Soffe
 */
public class StoreServer {

    private final HttpServer server;

    private final HomeApplianceDAO applianceDAO;
    private final CustomerDAO customerDAO;
    private final OrderDAO orderDAO;
    private final UserDAO userDAO;

    private final Basket basket;

  
    private boolean adminLoggedIn = false;

    public StoreServer(String dbUrl, int port) throws IOException {

        // Shared DAOs -single instances.
        this.applianceDAO = new HomeApplianceDAO(dbUrl);
        this.customerDAO = new CustomerDAO(dbUrl);
        this.orderDAO     = new OrderDAO(dbUrl);
        this.userDAO      = new UserDAO(dbUrl);

        this.basket = new Basket();

      
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

    
        configureRoutes();
    }

    //routing 

    private void configureRoutes() {

 // public handlers
        server.createContext("/products",
                new ProductHandler(applianceDAO, basket, this));

        server.createContext("/customers",
                new CustomerHandler(customerDAO, this));

        server.createContext("/basket",
                new BasketHandler(basket, customerDAO, orderDAO, this));

        
        server.createContext("/admin",
                new AdminHandler(userDAO, this));

       
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().set("Location", "/products");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
    }

   

    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    public void setAdminLoggedIn(boolean loggedIn) {
        this.adminLoggedIn = loggedIn;
    }

    public void start() {
        // SQLite requires single threaded writes
        server.setExecutor(Executors.newSingleThreadExecutor());

        server.start();

        System.out.println("Server running at http://localhost:" + 
                server.getAddress().getPort());
    }

    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:HomeStore_V2.db";
        int port = 8080;

        try {
            StoreServer server = new StoreServer(dbUrl, port);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}