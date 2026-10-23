package handlers;

import com.sun.net.httpserver.HttpExchange;
import dao.UserDAO;
import model.User;
import server.StoreServer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Handles /admin login/logout.
 * Simple proof-of-concept authentication.
 * @author Chloe Soffe
 */
public class AdminHandler extends BaseHandler {

    private final UserDAO userDAO;

    public AdminHandler(UserDAO userDAO, StoreServer server) {
        super(server);
        this.userDAO = userDAO;
    }

    @Override
    protected void doGet(HttpExchange exchange) throws IOException {
        String action = getAction(exchange);

        if ("logout".equals(action)) {
            server.setAdminLoggedIn(false);
            redirect(exchange, "/products");
            return;
        }

        showLoginForm(exchange);
    }

    @Override
    protected void doPost(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseFormBody(exchange);

        String username = form.getOrDefault("username", "");
        String password = form.getOrDefault("password", "");

       
        Optional<User> u = userDAO.findByUsername(username);

        if (!u.isPresent() || !u.get().getPassword().equals(password)) {
            sendHtml(exchange, 403, wrapHtml("Login Failed",
                "<h1>Login Failed</h1><p>Invalid username or password.</p>"
                + "<a class='btn' href='/admin'>Try Again</a>"
            ));
            return;
        }

        server.setAdminLoggedIn(true);
        redirect(exchange, "/products");
    }

    private void showLoginForm(HttpExchange exchange) throws IOException {
        String html =
            "<h1>Admin Login</h1>" +
            "<form method='POST' action='/admin'>" +
            "<label>Username:</label><input type='text' name='username' required>" +
            "<label>Password:</label><input type='password' name='password' required>" +
            "<button class='btn'>Login</button>" +
            "</form>";

        sendHtml(exchange, 200, wrapHtml("Admin Login", html));
    }
}