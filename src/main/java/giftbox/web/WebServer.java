package giftbox.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import giftbox.api.DiscountStrategy;
import giftbox.facade.GiftBoxFacade;
import giftbox.facade.GiftSelection;
import giftbox.facade.OrderReceipt;
import giftbox.observer.OrderStatus;
import giftbox.strategy.HolidayDiscount;
import giftbox.strategy.NoDiscount;
import giftbox.strategy.StudentDiscount;
import giftbox.strategy.ThresholdDiscount;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local classroom adapter. Business rules remain in the Java domain classes. */
public final class WebServer {
    private static final int MAX_BODY_BYTES = 16_384;
    private final GiftBoxFacade facade = new GiftBoxFacade();
    private final Map<String, OrderReceipt> orders = new LinkedHashMap<>();
    private final Path webRoot;

    private WebServer(Path webRoot) { this.webRoot = webRoot; }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        Path root = Path.of(args.length > 1 ? args[1] : "web").toAbsolutePath();
        if (!Files.isRegularFile(root.resolve("index.html"))) {
            throw new IllegalArgumentException("Web assets missing: run from the project folder or pass the web directory");
        }
        WebServer application = new WebServer(root);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", application::handle);
        server.start();
        System.out.println("GiftBox Studio: http://127.0.0.1:" + port);
        System.out.println("Local demonstration; orders reset when the server stops. Ctrl+C to stop.");
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) { handleApi(exchange, path); }
            else { serveAsset(exchange, path); }
        } catch (IllegalArgumentException | NullPointerException exception) {
            send(exchange, 400, "application/json", "{\"error\":" + Json.string(exception.getMessage() == null
                    ? "Invalid request" : exception.getMessage()) + "}");
        } catch (RuntimeException exception) {
            exception.printStackTrace();
            send(exchange, 500, "application/json", "{\"error\":\"Unexpected server error\"}");
        } finally { exchange.close(); }
    }

    private void handleApi(HttpExchange exchange, String path) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "POST");
            send(exchange, 405, "application/json", "{\"error\":\"Use POST\"}");
            return;
        }
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        String expectedOrigin = "http://127.0.0.1:" + exchange.getLocalAddress().getPort();
        if (origin != null && !origin.equals(expectedOrigin)) {
            send(exchange, 403, "application/json", "{\"error\":\"Origin not allowed\"}");
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.startsWith("application/x-www-form-urlencoded")) {
            send(exchange, 415, "application/json", "{\"error\":\"Use URL-encoded form data\"}");
            return;
        }
        byte[] bytes = exchange.getRequestBody().readNBytes(MAX_BODY_BYTES + 1);
        if (bytes.length > MAX_BODY_BYTES) {
            send(exchange, 413, "application/json", "{\"error\":\"Request is too large\"}");
            return;
        }
        Map<String, List<String>> form = parseForm(new String(bytes, StandardCharsets.UTF_8));
        switch (path) {
            case "/api/quote":
                send(exchange, 200, "application/json", Json.quote(facade.quote(value(form, "recipient"),
                        value(form, "message"), selections(form), strategy(value(form, "discount")))));
                break;
            case "/api/orders":
                if (orders.size() >= 1000) {
                    send(exchange, 409, "application/json", "{\"error\":\"Demo order limit reached; restart the server\"}");
                    return;
                }
                OrderReceipt receipt = facade.placeOrder(value(form, "recipient"), value(form, "message"),
                        selections(form), strategy(value(form, "discount")));
                orders.put(receipt.getId(), receipt);
                send(exchange, 201, "application/json", Json.receipt(receipt));
                break;
            case "/api/status":
                OrderReceipt existing = orders.get(value(form, "id"));
                if (existing == null) {
                    send(exchange, 404, "application/json", "{\"error\":\"Order not found; it may have expired after restart\"}");
                    return;
                }
                existing.advanceTo(OrderStatus.valueOf(value(form, "status")));
                send(exchange, 200, "application/json", Json.receipt(existing));
                break;
            default: send(exchange, 404, "application/json", "{\"error\":\"Endpoint not found\"}");
        }
    }

    private List<GiftSelection> selections(Map<String, List<String>> form) {
        List<GiftSelection> result = new ArrayList<>();
        for (String item : form.getOrDefault("item", List.of())) {
            String[] parts = item.split(":", -1);
            if (parts.length != 3 || !parts[1].matches("[01]") || !parts[2].matches("[01]")) {
                throw new IllegalArgumentException("Invalid gift selection");
            }
            result.add(new GiftSelection(parts[0], parts[1].equals("1"), parts[2].equals("1")));
        }
        return result;
    }

    private DiscountStrategy strategy(String name) {
        switch (name) {
            case "none": return new NoDiscount();
            case "student": return new StudentDiscount();
            case "holiday": return new HolidayDiscount();
            case "threshold": return new ThresholdDiscount();
            default: throw new IllegalArgumentException("Unknown discount");
        }
    }

    private static Map<String, List<String>> parseForm(String body) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
        }
        return result;
    }

    private static String value(Map<String, List<String>> form, String key) {
        List<String> values = form.getOrDefault(key, List.of(""));
        if (values.size() != 1) { throw new IllegalArgumentException("Duplicate field: " + key); }
        return values.get(0);
    }

    private void serveAsset(HttpExchange exchange, String path) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain", "Use GET");
            return;
        }
        Map<String, String> assets = Map.of("/", "index.html", "/index.html", "index.html",
                "/style.css", "style.css", "/app.js", "app.js");
        String file = assets.get(path);
        if (file == null) { send(exchange, 404, "text/plain", "Not found"); return; }
        String type = file.endsWith(".css") ? "text/css" : file.endsWith(".js") ? "text/javascript" : "text/html";
        send(exchange, 200, type, Files.readString(webRoot.resolve(file), StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int status, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; frame-ancestors 'none'");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
