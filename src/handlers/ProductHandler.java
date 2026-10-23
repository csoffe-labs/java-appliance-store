package handlers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;

import dao.HomeApplianceDAO;
import model.Basket;
import model.HomeAppliance;
import server.StoreServer;

/**
 * Handles all product web actions.
 *listing, searching, adding, editing and deleting
 * products, as well as customer actions for adding items
 * to the basket
 * @author Chloe Soffe
 */

public class ProductHandler extends BaseHandler {

    private final HomeApplianceDAO dao;
    private final Basket basket;

    public ProductHandler(HomeApplianceDAO dao, Basket basket, StoreServer server) {
        super(server);
        this.dao = dao;
        this.basket = basket;
    }

    @Override
    protected void doGet(HttpExchange ex) throws IOException {
        String action = getAction(ex);

       // Admin-only actions
        switch (action) {
            case "add":
            case "edit":
            case "delete":
                if (!server.isAdminLoggedIn()) {
                    sendHtml(ex, 403, errorPage("Admin access required."));
                    return;
                }
                break;
        }

        switch (action) {
            case "add":         showAddForm(ex); break;
            case "edit":        showEditForm(ex); break;
            case "delete":      doDelete(ex); break;
            case "addToBasket": doAddToBasket(ex); break;
            default:            showList(ex);
        }
    }

    @Override
    protected void doPost(HttpExchange ex) throws IOException {
        if (!server.isAdminLoggedIn()) {
            sendHtml(ex, 403, errorPage("Admin access required."));
            return;
        }

        switch (getAction(ex)) {
            case "add":  processAdd(ex); break;
            case "edit": processEdit(ex); break;
            default:
                sendHtml(ex, 400, errorPage("Unknown action."));
        }
    }

    //prodcurt lists
    private void showList(HttpExchange ex) throws IOException {

        String q = getQueryParam(ex, "search");
        List<HomeAppliance> products = dao.searchByDescription(q);

        StringBuilder body = new StringBuilder();
        body.append("<h1>Products</h1>");

        body.append("<form method='GET' action='/products' style='margin-bottom:15px;'>"
                + "<input type='text' name='search' placeholder='Search description...' "
                + "value='" + escapeHtml(q == null ? "" : q) + "'>"
                + "<button class='btn' type='submit'>Search</button>"
                + "</form>");
        

        // Basket summary
        int itemCount = basket.getTotalItemCount();  

        if (itemCount > 0) {
            body.append("<div class='flash'>Basket: ")
                .append(itemCount)
                .append(" item(s) - Total £")
                .append(String.format("%.2f", basket.getTotal() / 100.0))  
                .append(" <a class='btn btn-success' href='/basket'>View Basket</a></div>");
        }

     
        if (server.isAdminLoggedIn()) {
            body.append("<a class='btn' href='/products?action=add'>Add New Product</a><br><br>");
        }

      
        body.append("<table>");
        body.append("<tr>");
        body.append("<th>ID</th><th>SKU</th><th>Brand</th><th>Model</th>");
        body.append("<th>Description</th><th>Category</th><th>Price</th><th>Stock</th>");

        if (!server.isAdminLoggedIn()) {
            body.append("<th>Qty</th><th>Warranty</th><th>Add</th>");
        } else {
            body.append("<th>Admin</th>");
        }

        body.append("</tr>");

        // All table rows
        for (HomeAppliance p : products) {
            body.append("<tr>");
            body.append("<td>").append(p.getId()).append("</td>");
            body.append("<td>").append(escapeHtml(p.getSku())).append("</td>");
            body.append("<td>").append(escapeHtml(p.getBrand())).append("</td>");
            body.append("<td>").append(escapeHtml(p.getModel())).append("</td>");
            body.append("<td>").append(escapeHtml(p.getDescription())).append("</td>");
            body.append("<td>").append(escapeHtml(p.getCategory())).append("</td>");
            body.append("<td>£").append(String.format("%.2f", p.getPrice() / 100.0)).append("</td>");
            body.append("<td>").append(p.getStockLevel()).append("</td>");

            // Customer mode (Qty + Warranty + Add)
            if (!server.isAdminLoggedIn()) {

                if (p.getStockLevel() > 0) {
                   
                    body.append("<td>")
                        .append("<input type='number' name='qty' form='f").append(p.getId())
                        .append("' value='1' min='1' max='").append(p.getStockLevel())
                        .append("' style='width:55px;padding:6px;'>")
                        .append("</td>");

                 
                    body.append("<td>")
                        .append("<select name='warranty' form='f").append(p.getId()).append("' style='padding:6px;'>")
                        .append("<option value='1'>1 yr</option>")
                        .append("<option value='2'>2 yr</option>")
                        .append("<option value='3'>3 yr</option>")
                        .append("</select>")
                        .append("</td>");

                 
                    body.append("<td>")
                        .append("<form id='f").append(p.getId())
                        .append("' method='GET' action='/products' style='margin:0;'>")
                        .append("<input type='hidden' name='action' value='addToBasket'>")
                        .append("<input type='hidden' name='id' value='").append(p.getId()).append("'>")
                        .append("<button class='btn btn-success' type='submit'>Add</button>")
                        .append("</form>")
                        .append("</td>");

                } else {
                    body.append("<td colspan='3' style='color:#999;'>Out of Stock</td>");
                }

            } else {
            	
   // ADMIN MODE ONLY: Edit / Delete /Add
                body.append("<td>")
                    .append("<a class='btn' href='/products?action=edit&id=")
                    .append(p.getId()).append("'>Edit</a> ")

                    .append("<a class='btn btn-danger' href='/products?action=delete&id=")
                    .append(p.getId())
                    .append("' onclick=\"return confirm('Delete this product?')\">Delete</a>")
                    .append("</td>");
            }

            body.append("</tr>");
        }

        body.append("</table>");
        sendHtml(ex, 200, wrapHtml("Products", body.toString()));
    }

    //forms

    private void showAddForm(HttpExchange ex) throws IOException {
        String html = "<h1>Add Product</h1>" +
            "<form method='POST' action='/products?action=add'>" +
            renderProductFields(null) +
            "<button class='btn' type='submit'>Save Product</button> " +
            "<a class='btn' href='/products'>Cancel</a></form>";

        sendHtml(ex, 200, wrapHtml("Add Product", html));
    }

    private void showEditForm(HttpExchange ex) throws IOException {
        int id = parseIntOrDefault(getQueryParam(ex, "id"), -1);

        Optional<HomeAppliance> maybe = dao.findById(id);
        if (!maybe.isPresent()) {
            sendHtml(ex, 404, errorPage("Product not found."));
            return;
        }

        HomeAppliance p = maybe.get();

        String html = "<h1>Edit Product</h1>" +
            "<form method='POST' action='/products?action=edit'>" +
            "<input type='hidden' name='id' value='" + p.getId() + "'>" +
            renderProductFields(p) +
            "<button class='btn' type='submit'>Update Product</button> " +
            "<a class='btn' href='/products'>Cancel</a></form>";

        sendHtml(ex, 200, wrapHtml("Edit Product", html));
    }
//crud action routing

    private void doDelete(HttpExchange ex) throws IOException {
        int id = parseIntOrDefault(getQueryParam(ex, "id"), -1);
        if (id > 0) dao.delete(id);
        redirect(ex, "/products");
    }

    private void doAddToBasket(HttpExchange ex) throws IOException {
        int id = parseIntOrDefault(getQueryParam(ex, "id"), -1);
        int qty = parseIntOrDefault(getQueryParam(ex, "qty"), 1);
        int warranty = parseIntOrDefault(getQueryParam(ex, "warranty"), 1);

        Optional<HomeAppliance> maybe = dao.findById(id);
        if (!maybe.isPresent()) {
            sendHtml(ex, 404, errorPage("Product not found."));
            return;
        }

        HomeAppliance p = maybe.get();

        if (qty <= 0 || qty > p.getStockLevel()) {
            sendHtml(ex, 400, errorPage("Invalid quantity or insufficient stock."));
            return;
        }

        basket.addItem(p, qty, warranty);
        redirect(ex, "/products");
    }

    private void processAdd(HttpExchange ex) throws IOException {
        Map<String,String> f = parseFormBody(ex);

        HomeAppliance newP = HomeAppliance.createNew(
            f.get("sku"),
            f.get("brand"),
            f.get("model"),
            f.get("description"),
            f.get("category"),
            parseIntOrDefault(f.get("price"), 0),
            parseIntOrDefault(f.get("stock"), 0)
        );

        dao.insert(newP);
        redirect(ex, "/products");
    }

    private void processEdit(HttpExchange ex) throws IOException {
        Map<String,String> f = parseFormBody(ex);
        int id = parseIntOrDefault(f.get("id"), -1);

        HomeAppliance updated = HomeAppliance.forUpdate(
            id,
            f.get("sku"),
            f.get("brand"),
            f.get("model"),
            f.get("description"),
            f.get("category"),
            parseIntOrDefault(f.get("price"), 0),
            parseIntOrDefault(f.get("stock"), 0)
        );

        dao.update(updated);
        redirect(ex, "/products");
    }

  //form rendering

    private String renderProductFields(HomeAppliance p) {
        String sku = p == null ? "" : escapeHtml(p.getSku());
        String brand = p == null ? "" : escapeHtml(p.getBrand());
        String model = p == null ? "" : escapeHtml(p.getModel());
        String desc = p == null ? "" : escapeHtml(p.getDescription());
        String category = p == null ? "" : escapeHtml(p.getCategory());
        String price = p == null ? "" : String.valueOf(p.getPrice());
        String stock = p == null ? "" : String.valueOf(p.getStockLevel());

        return ""
            + "<label>SKU:</label><input name='sku' value='" + sku + "' required>"
            + "<label>Brand:</label><input name='brand' value='" + brand + "' required>"
            + "<label>Model:</label><input name='model' value='" + model + "' required>"
            + "<label>Description:</label><textarea name='description'>" + desc + "</textarea>"
            + "<label>Category:</label><input name='category' value='" + category + "'>"
            + "<label>Price (pence):</label><input type='number' name='price' value='" + price + "' required>"
            + "<label>Stock:</label><input type='number' name='stock' value='" + stock + "' required>";
    }
}