package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.StoreServer;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Shared base class for all handlers
 * Provides the common HTML rendering and any redirects
 * Form parsing and error helpers

 * @author Chloe Soffe
 */

public abstract class BaseHandler implements HttpHandler {

    protected final StoreServer server;

    public BaseHandler(StoreServer server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                doGet(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                doPost(exchange);
            } else {
                sendHtml(exchange, 405, errorPage("Method Not Allowed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendHtml(exchange, 500, errorPage("Error: " + e.getMessage()));
        }
    }

    protected abstract void doGet(HttpExchange exchange) throws IOException;
    protected abstract void doPost(HttpExchange exchange) throws IOException;

    // Query & Form parsing
    protected Map<String,String> parseQuery(String query) {
        Map<String,String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;

        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = decode(pair.substring(0, idx));
                String val = decode(pair.substring(idx + 1));
                map.put(key, val);
            }
        }
        return map;
    }

    protected Map<String,String> parseFormBody(HttpExchange ex) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8)
        );
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) body.append(line);
        return parseQuery(body.toString());
    }

    private String decode(String v) {
        try { return URLDecoder.decode(v, "UTF-8"); }
        catch (Exception e) { return v; }
    }

    protected String getQueryParam(HttpExchange ex, String name) {
        return parseQuery(ex.getRequestURI().getQuery()).get(name);
    }

    protected String getAction(HttpExchange ex) {
        String a = getQueryParam(ex, "action");
        return (a != null) ? a : "list";
    }

    // Response helpers
    protected void sendHtml(HttpExchange ex, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    // HTML wrapper with simple, clean CSS
    protected String wrapHtml(String title, String body) {
        return "<!DOCTYPE html><html><head>" +
            "<meta charset='UTF-8'>" +
            "<title>" + escapeHtml(title) + "</title>" +
            "<style>" +
            "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; " +
            "       max-width: 1200px; margin: 0 auto; padding: 20px; background: #f5f5f5; }" +
            "nav { background: white; padding: 15px; margin: -20px -20px 20px; " +
            "      border-bottom: 2px solid #007bff; }" +
            "nav a { margin-right: 20px; text-decoration: none; color: #007bff; " +
            "        font-weight: 500; }" +
            "nav a:hover { text-decoration: underline; }" +
            "h1 { color: #333; margin-bottom: 20px; }" +
            "table { width: 100%; background: white; border-collapse: collapse; " +
            "        margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }" +
            "th { background: #007bff; color: white; padding: 12px; text-align: left; }" +
            "td { padding: 12px; border-bottom: 1px solid #eee; }" +
            "tr:hover { background: #f8f9fa; }" +
            ".btn { display: inline-block; padding: 8px 16px; " +
            "       background: #007bff; color: white; text-decoration: none; " +
            "       border: none; cursor: pointer; margin-right: 5px; " +
            "       border-radius: 3px; font-size: 14px; }" +
            ".btn:hover { background: #0056b3; }" +
            ".btn-danger { background: #dc3545; }" +
            ".btn-danger:hover { background: #c82333; }" +
            ".btn-success { background: #28a745; }" +
            ".btn-success:hover { background: #218838; }" +
            "input, select, textarea { padding: 8px; margin: 5px 0; width: 100%; " +
            "                          max-width: 300px; border: 1px solid #ddd; " +
            "                          border-radius: 3px; }" +
            "label { display: block; margin-top: 10px; font-weight: 500; color: #555; }" +
            "form { background: white; padding: 20px; border-radius: 5px; " +
            "       box-shadow: 0 1px 3px rgba(0,0,0,0.1); }" +
            ".flash { padding: 10px; background: #d4edda; border: 1px solid #c3e6cb; " +
            "         color: #155724; margin-bottom: 20px; border-radius: 3px; }" +
            ".error { padding: 15px; background: #f8d7da; color: #721c24; " +
            "         border: 1px solid #f5c6cb; margin: 20px 0; border-radius: 3px; }" +
            ".success { padding: 15px; background: #d4edda; color: #155724; " +
            "           border: 1px solid #c3e6cb; margin: 20px 0; border-radius: 3px; }" +
            "</style>" +
            "</head><body>" +
            "<nav>" +
            "<a href='/products'>Products</a>" +
            "<a href='/customers'>Customers</a>" +
            "<a href='/basket'>Basket</a>" +
            (server.isAdminLoggedIn()
                ? "<a href='/admin?action=logout' style='float:right;color:#dc3545;'>Logout (Admin)</a>"
                : "<a href='/admin' style='float:right;'>Admin Login</a>") +
            "</nav>" +
            "<div class='container'>" + body + "</div>" +
            "</body></html>";
    }

    protected String errorPage(String msg) {
        return wrapHtml("Error",
            "<div class='error'>" + escapeHtml(msg) + "</div>");
    }

    protected String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;")
                .replace("'","&#39;"); //  attribute safety
    }

    protected int parseIntOrDefault(String v, int def) {
        try { return (v == null) ? def : Integer.parseInt(v.trim()); }
        catch (Exception e) { return def; }
    }

    protected Optional<Integer> parseInt(String v) {
        try { return (v == null || v.trim().isEmpty()) ? Optional.empty() : Optional.of(Integer.parseInt(v)); }
        catch (Exception e) { return Optional.empty(); }
    }
}