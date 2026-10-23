package handlers;

import com.sun.net.httpserver.HttpExchange;
import dao.CustomerDAO;
import model.Address;
import model.Customer;
import server.StoreServer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Web handler for customer management and CRUD
 * @author Chloe Soffe
 */
public class CustomerHandler extends BaseHandler {

    private final CustomerDAO dao;

    public CustomerHandler(CustomerDAO dao, StoreServer server) {
        super(server);
        this.dao = dao;
    }
   

    @Override
    protected void doGet(HttpExchange exchange) throws IOException {
        String action = getAction(exchange);

 // Admin ops
        switch (action) {
            case "add":
            case "edit":
            case "delete":
                if (!server.isAdminLoggedIn()) {
                    sendHtml(exchange, 403, errorPage("Admin access required."));
                    return;
                }
                break;
        }

        switch (action) {
            case "add":    showAddForm(exchange); break;
            case "edit":   showEditForm(exchange); break;
            case "delete": doDelete(exchange); break;
            default:       showList(exchange);
        }
    }

    

    private void showList(HttpExchange exchange) throws IOException {
        List<Customer> customers = dao.findAll();

        StringBuilder html = new StringBuilder("<h1>Customers</h1>\n")
            .append("<a class='btn' href='/customers?action=add'>Add New Customer</a>\n");

        if (customers.isEmpty()) {
            html.append("<p>No customers found.</p>\n");

        } else {
            html.append("<table>\n<tr>")
                .append("<th>ID</th><th>Business Name</th><th>Telephone</th>")
                .append("<th>Address</th><th>Actions</th></tr>\n");

            for (Customer c : customers) {
                html.append("<tr>")
                    .append("<td>").append(c.getId()).append("</td>")
                    .append("<td>").append(escapeHtml(c.getBusinessName())).append("</td>")
                    .append("<td>").append(escapeHtml(c.getTelephone())).append("</td>")
                    .append("<td>").append(escapeHtml(c.getAddress().asSingleLine())).append("</td>")
                    .append("<td>");

                if (server.isAdminLoggedIn()) {
                    html.append("<a class='btn' href='/customers?action=edit&id=")
                        .append(c.getId()).append("'>Edit</a> ")

                        .append("<a class='btn btn-danger' href='/customers?action=delete&id=")
                        .append(c.getId())
                        .append("' onclick=\"return confirm('Delete this customer?')\">Delete</a>");
                }

                html.append("</td>")
                    .append("</tr>\n");
            }

            html.append("</table>\n");
        }

        sendHtml(exchange, 200, wrapHtml("Customers", html.toString()));
    }

   

    private void showAddForm(HttpExchange exchange) throws IOException {
        StringBuilder html = new StringBuilder("<h1>Add New Customer</h1>\n")
            .append("<form method='POST' action='/customers?action=add'>\n")
            .append(customerFormFields(null))
            .append("<button type='submit' class='btn'>Save Customer</button>\n")
            .append("<a class='btn' href='/customers'>Cancel</a>\n")
            .append("</form>\n");

        sendHtml(exchange, 200, wrapHtml("Add Customer", html.toString()));
    }

    //edit forms + shared

    private void showEditForm(HttpExchange exchange) throws IOException {
        int id = parseIntOrDefault(getQueryParam(exchange, "id"), -1);
        if (id < 0) {
            sendHtml(exchange, 400, errorPage("Invalid customer ID"));
            return;
        }

        Optional<Customer> maybe = dao.findById(id);
        if (!maybe.isPresent()) {
            sendHtml(exchange, 404, errorPage("Customer not found"));
            return;
        }

        Customer c = maybe.get();

        StringBuilder html = new StringBuilder("<h1>Edit Customer</h1>\n")
            .append("<form method='POST' action='/customers?action=edit'>\n")
            .append("<input type='hidden' name='id' value='").append(c.getId()).append("'>\n")
            .append(customerFormFields(c))
            .append("<button type='submit' class='btn'>Update Customer</button>\n")
            .append("<a class='btn' href='/customers'>Cancel</a>\n")
            .append("</form>\n");

        sendHtml(exchange, 200, wrapHtml("Edit Customer", html.toString()));
    }

    

    private String customerFormFields(Customer c) {
        String bn   = (c != null) ? escapeHtml(c.getBusinessName()) : "";
        String tel  = (c != null) ? escapeHtml(c.getTelephone()) : "";

        Address addr = (c != null) ? c.getAddress() : new Address("", "", "", "", "");

        return new StringBuilder()
            .append("<label>Business Name:</label>\n")
            .append("<input type='text' name='businessName' value='").append(bn).append("' required>\n")

            .append("<label>Telephone:</label>\n")
            .append("<input type='text' name='telephone' value='").append(tel).append("'>\n")

            .append("<h3>Address</h3>\n")

            .append("<label>Line 1:</label>\n")
            .append("<input type='text' name='line1' value='").append(escapeHtml(addr.getLine1())).append("'>\n")

            .append("<label>Line 2:</label>\n")
            .append("<input type='text' name='line2' value='").append(escapeHtml(addr.getLine2())).append("'>\n")

            .append("<label>Town:</label>\n")
            .append("<input type='text' name='town' value='").append(escapeHtml(addr.getTown())).append("'>\n")

            .append("<label>Country:</label>\n")
            .append("<input type='text' name='country' value='").append(escapeHtml(addr.getCountry())).append("'>\n")

            .append("<label>Post Code:</label>\n")
            .append("<input type='text' name='postCode' value='").append(escapeHtml(addr.getPostCode())).append("'>\n")
            .toString();
    }

    //delete route

    private void doDelete(HttpExchange exchange) throws IOException {
        int id = parseIntOrDefault(getQueryParam(exchange, "id"), -1);

        if (id < 0) {
            sendHtml(exchange, 400, errorPage("Invalid customer ID"));
            return;
        }

        if (dao.delete(id)) {
            redirect(exchange, "/customers");
        } else {
            sendHtml(exchange, 404, errorPage("Customer not found or could not be deleted"));
        }
    }

    @Override
    protected void doPost(HttpExchange exchange) throws IOException {
        if (!server.isAdminLoggedIn()) {
            sendHtml(exchange, 403, errorPage("Admin access required."));
            return;
        }

        switch (getAction(exchange)) {
            case "add":  processAdd(exchange); break;
            case "edit": processEdit(exchange); break;
            default:
                sendHtml(exchange, 400, errorPage("Unknown action"));
        }
    }

    			//create

    private void processAdd(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseFormBody(exchange);

        try {
            Address addr = new Address(
                form.getOrDefault("line1", ""),
                form.getOrDefault("line2", ""),
                form.getOrDefault("town", ""),
                form.getOrDefault("country", ""),
                form.getOrDefault("postCode", "")
            );

            Customer c = new Customer(
                0,
                form.get("businessName"),
                form.getOrDefault("telephone", ""),
                addr
            );

            dao.insert(c);
            redirect(exchange, "/customers");

        } catch (IllegalArgumentException ex) {
            sendHtml(exchange, 400, errorPage("Validation error: " + ex.getMessage()));
        }
    }

    //update

    private void processEdit(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseFormBody(exchange);
        int id = parseIntOrDefault(form.get("id"), -1);

        if (id < 0) {
            sendHtml(exchange, 400, errorPage("Invalid customer ID"));
            return;
        }

        try {
            Address addr = new Address(
                form.getOrDefault("line1", ""),
                form.getOrDefault("line2", ""),
                form.getOrDefault("town", ""),
                form.getOrDefault("country", ""),
                form.getOrDefault("postCode", "")
            );

            Customer updated = new Customer(
                id,
                form.get("businessName"),
                form.getOrDefault("telephone", ""),
                addr
            );

            if (dao.update(updated)) {
                redirect(exchange, "/customers");
            } else {
                sendHtml(exchange, 404, errorPage("Customer not found"));
            }

        } catch (IllegalArgumentException ex) {
            sendHtml(exchange, 400, errorPage("Validation error: " + ex.getMessage()));
        }
    }
}