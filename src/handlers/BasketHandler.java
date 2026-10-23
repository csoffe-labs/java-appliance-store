package handlers;

import com.sun.net.httpserver.HttpExchange;
import dao.CustomerDAO;
import dao.OrderDAO;
import model.ApplianceItem;
import model.Basket;
import model.Customer;
import model.Order;
import model.OrderItem;
import server.StoreServer;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages basket activity in the web UI
 * applying simple sorting
 * removing entries, clearing the basket
 * Guiding the user through checkout
 * HTML in line
 * @author Chloe Soffe
 */
public class BasketHandler extends BaseHandler {

    private final Basket basket;
    private final CustomerDAO customerDAO;
    private final OrderDAO orderDAO;

    public BasketHandler(Basket basket, CustomerDAO customerDAO, OrderDAO orderDAO, StoreServer server) {
        super(server);        // passes server up to BaseHandler
        this.basket = basket;
        this.customerDAO = customerDAO;
        this.orderDAO = orderDAO;
    
    }

   
    @Override
    protected void doGet(HttpExchange exchange) throws IOException {
        String action = getAction(exchange);

        switch (action) {
            case "remove":       doRemove(exchange); break;
            case "clear":        doClear(exchange); break;
            case "checkout":     showCheckout(exchange); break;
            case "sortPrice":
                basket.sortByPrice();
                redirect(exchange, "/basket");
                break;
            case "sortWarranty":
                basket.sortByWarranty();
                redirect(exchange, "/basket");
                break;
            default:
                showBasket(exchange);
        }
    }

 //basket page routing

    private void showBasket(HttpExchange exchange) throws IOException {
        List<ApplianceItem> items = basket.getItems();
        StringBuilder html = new StringBuilder("<h1>Shopping Basket</h1>\n");

        if (!basket.hasItems()) { //domain
        	
            html.append("<p>Your basket is empty.</p>\n")
                .append("<a class='btn' href='/products'>Browse Products</a>\n");
            sendHtml(exchange, 200, wrapHtml("Basket", html.toString()));
            return;
        }

     
        html.append("<div style='margin-bottom:10px'>")
            .append("<a class='btn' href='/basket?action=sortPrice'>Sort by Price</a>\n")
            .append("<a class='btn' href='/basket?action=sortWarranty'>Sort by Warranty</a>\n")
            .append("<a class='btn btn-danger' href='/basket?action=clear' ")
            .append("onclick=\"return confirm('Clear entire basket?')\">Clear Basket</a>\n")
            .append("</div>\n");

      
        html.append("<table>\n<tr>")
            .append("<th>#</th><th>Product</th><th>Unit Price</th>")
            .append("<th>Qty</th><th>Warranty</th><th>Line Total</th><th>Actions</th>")
            .append("</tr>\n");

        int index = 0;
        for (ApplianceItem item : items) {
            html.append("<tr>")
                .append("<td>").append(index + 1).append("</td>")
                .append("<td>")
                    .append(escapeHtml(item.getProduct().getBrand())).append(" ")
                    .append(escapeHtml(item.getProduct().getModel()))
                .append("</td>")
                .append("<td>£").append(formatMoney(item.getProduct().getPrice())).append("</td>")
                .append("<td>").append(item.getQuantity()).append("</td>")
                .append("<td>").append(item.getWarrantyYears()).append(" yr</td>")
                .append("<td>£").append(formatMoney(item.getLineTotal())).append("</td>")
                .append("<td><a class='btn btn-danger' href='/basket?action=remove&index=")
                    .append(index).append("'>Remove</a></td>")
                .append("</tr>\n");
            index++;
        }

        // Total
        html.append("<tr style='font-weight:bold'>")
            .append("<td colspan='5' style='text-align:right'>Total:</td>")
            .append("<td>£").append(formatMoney(basket.getTotal())).append("</td>")
            .append("<td></td></tr>\n</table>\n");

        // Footer
        html.append("<div style='margin-top:20px'>")
            .append("<a class='btn' href='/products'>Continue Shopping</a>\n")
            .append("<a class='btn' href='/basket?action=checkout' ")
            .append("style='background:#060;color:#fff'>Proceed to Checkout</a>\n")
            .append("</div>\n");

        sendHtml(exchange, 200, wrapHtml("Basket", html.toString()));
    }

   //checkout page routes

    private void showCheckout(HttpExchange exchange) throws IOException {
    	if (!basket.hasItems()) {
    	    redirect(exchange, "/basket");
    	    return;
    	}

        List<Customer> customers = customerDAO.findAll();

        StringBuilder html = new StringBuilder("<h1>Checkout</h1>\n");

        // Summary table
        html.append("<h2>Order Summary</h2>\n<table>\n")
            .append("<tr><th>Product</th><th>Qty</th><th>Warranty</th><th>Line Total</th></tr>\n");

        for (ApplianceItem item : basket.getItems()) {
            html.append("<tr>")
                .append("<td>")
                    .append(escapeHtml(item.getProduct().getBrand())).append(" ")
                    .append(escapeHtml(item.getProduct().getModel()))
                .append("</td>")
                .append("<td>").append(item.getQuantity()).append("</td>")
                .append("<td>").append(item.getWarrantyYears()).append(" yr</td>")
                .append("<td>£").append(formatMoney(item.getLineTotal())).append("</td>")
                .append("</tr>\n");
        }

        html.append("<tr style='font-weight:bold'>")
            .append("<td colspan='3' style='text-align:right'>Total:</td>")
            .append("<td>£").append(formatMoney(basket.getTotal())).append("</td>")
            .append("</tr>\n</table>\n");

        // Customers
        html.append("<h2>Select Customer</h2>\n");

        if (customers.isEmpty()) {
            html.append("<p>No customers available. ")
                .append("<a href='/customers?action=add'>Add one</a> first.</p>");
            sendHtml(exchange, 200, wrapHtml("Checkout", html.toString()));
            return;
        }

        html.append("<form method='POST' action='/basket?action=placeOrder'>\n")
            .append("<label>Customer:</label>\n<select name='customerId' required>\n")
            .append("<option value=''>-- Select Customer --</option>\n");

        for (Customer c : customers) {
            html.append("<option value='").append(c.getId()).append("'>")
                .append(escapeHtml(c.getBusinessName()))
                .append("</option>\n");
        }

        html.append("</select><br><br>\n")
            .append("<button type='submit' class='btn' style='background:#060;color:#fff'>Place Order</button>\n")
            .append("<a class='btn' href='/basket'>Back to Basket</a>\n")
            .append("</form>\n");

        sendHtml(exchange, 200, wrapHtml("Checkout", html.toString()));
    }

   //ensures remove and clear

    private void doRemove(HttpExchange exchange) throws IOException {
        int index = parseIntOrDefault(getQueryParam(exchange, "index"), -1);
        if (index >= 0 && index < basket.getItems().size()) {
            basket.removeItem(index);
        }
        redirect(exchange, "/basket");
    }

    private void doClear(HttpExchange exchange) throws IOException {
        basket.clear();
        redirect(exchange, "/basket");
    }


    @Override
    protected void doPost(HttpExchange exchange) throws IOException {
        if ("placeOrder".equals(getAction(exchange))) {
            processPlaceOrder(exchange);
        } else {
            sendHtml(exchange, 400, errorPage("Unknown action"));
        }
    }

    

    private void processPlaceOrder(HttpExchange exchange) throws IOException {
       
        if (!basket.hasItems()) {  
            redirect(exchange, "/basket");
            return;
        }

        Map<String,String> form = parseFormBody(exchange);
        int customerId = parseIntOrDefault(form.get("customerId"), -1);

        if (customerId < 0 || !customerDAO.findById(customerId).isPresent()) {
            sendHtml(exchange, 400, errorPage("Please select a valid customer"));
            return;
        }

        try {
    // basket items to order items
            List<OrderItem> items = new ArrayList<>();
            for (ApplianceItem item : basket.getItems()) {
                items.add(item.toOrderItem(0)); // placeholder orderId
            }

            Order newOrder = Order.createNew(customerId, LocalDate.now(), basket.getTotal());
            Order savedOrder = orderDAO.createCompleteOrder(newOrder, items);

            basket.clear();

    // Confirmation
            StringBuilder html = new StringBuilder("<h1>Order Placed!</h1>\n")
                .append("<div class='success'>")
                .append("<p>Order ID: ").append(savedOrder.getId()).append("</p>\n")
                .append("<p>Total: £").append(formatMoney(savedOrder.getTotal())).append("</p>\n")
                .append("</div>\n")
                .append("<a class='btn' href='/products'>Continue Shopping</a>\n");

            sendHtml(exchange, 200, wrapHtml("Order Confirmation", html.toString()));

        } catch (Exception e) {
            sendHtml(exchange, 500, errorPage("Failed to place order: " + e.getMessage()));
        }
    }

    //  helper for money formatting
    private String formatMoney(int pennies) {
        return String.format("%.2f", pennies / 100.0);
    }
}