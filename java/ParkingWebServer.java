import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParkingWebServer {
    private final ParkingManager manager = new ParkingManager();
    private final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        new ParkingWebServer().start();
    }

    private void start() throws IOException {
        HttpServer server = HttpServer.create(
            new InetSocketAddress("127.0.0.1", 8080), 0);

        server.createContext("/api/state", this::handleState);
        server.createContext("/api/action", this::handleAction);
        server.createContext("/", this::handleFile);
        server.start();

        System.out.println("Parking server: http://127.0.0.1:8080");
        System.out.println("Check connection: http://127.0.0.1:8080/api/state");
        System.out.println("Press Ctrl+C to stop. Records are stored in memory.");
    }

    private void handleState(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendJson(exchange, 405, Map.of("message", "Use GET."));
            return;
        }

        synchronized (manager) {
            sendJson(exchange, 200, state());
        }
    }

    private void handleAction(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendJson(exchange, 405, Map.of("message", "Use POST."));
            return;
        }

        try {
            byte[] body = exchange.getRequestBody().readNBytes(8193);
            if (body.length > 8192) {
                throw new IllegalArgumentException("Request is too large.");
            }

            JsonObject request = gson.fromJson(
                new String(body, StandardCharsets.UTF_8), JsonObject.class);

            if (request == null) {
                throw new IllegalArgumentException("Request is required.");
            }

            synchronized (manager) {
                String action = required(request, "action");
                String message;

                switch (action) {
                    case "park" -> {
                        String plate = required(request, "plate");
                        Vehicle vehicle = switch (required(request, "type")) {
                            case "Motorcycle" -> new Vehicle.Motorcycle(plate);
                            case "Car" -> new Vehicle.Car(plate);
                            case "Bus" -> new Vehicle.Bus(plate);
                            default -> throw new IllegalArgumentException(
                                "Unknown vehicle type.");
                        };
                        manager.parkVehicle(vehicle, required(request, "space"));
                        message = "Vehicle parked.";
                    }
                    case "move" -> {
                        manager.moveVehicle(
                            required(request, "plate"),
                            required(request, "space"));
                        message = "Vehicle moved.";
                    }
                    case "depart" -> {
                        int released = manager.removeVehicle(
                            required(request, "plate"));
                        message = "Vehicle departed. Spaces released: " + released;
                    }
                    case "search" -> {
                        List<String> locations = new VehicleSearch().findSpaces(
                            manager.getSpaces(), required(request, "plate"));
                        message = locations.isEmpty()
                            ? "Vehicle not found."
                            : "Location: " + String.join(", ", locations);
                    }
                    default -> throw new IllegalArgumentException(
                        "Unknown action.");
                }

                Map<String, Object> response = state();
                response.put("message", message);
                sendJson(exchange, 200, response);
            }
        } catch (IllegalArgumentException | JsonParseException
                 | IllegalStateException | UnsupportedOperationException error) {
            sendJson(exchange, 400, Map.of(
                "message", "Action blocked: " + error.getMessage()));
        }
    }

    private String required(JsonObject request, String name) {
        if (!request.has(name) || request.get(name).isJsonNull()
                || !request.get(name).isJsonPrimitive()
                || !request.getAsJsonPrimitive(name).isString()) {
            throw new IllegalArgumentException(name + " must be text.");
        }

        String value = request.get(name).getAsString().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value;
    }

    private Map<String, Object> state() {
        List<Map<String, Object>> spaces = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();

        for (ParkingSpace space : manager.getSpacesOrdered()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", space.getId());
            item.put("level", space.getLevel());
            item.put("row", space.getRow());
            item.put("number", space.getNumber());
            item.put("type", space.getType().name());
            item.put("available", space.isAvailable());
            item.put("plate", space.isAvailable()
                ? "" : space.getVehicle().getPlateNumber());
            item.put("vehicleType", space.isAvailable()
                ? "" : space.getVehicle().getType());
            spaces.add(item);

            if (!space.isAvailable() && !vehicles.contains(space.getVehicle())) {
                vehicles.add(space.getVehicle());
            }
        }

        Comparator<Vehicle> comparator = Comparator
            .comparing(Vehicle::getType)
            .thenComparing(Vehicle::getPlateNumber);

        List<Map<String, String>> sortedVehicles = new ArrayList<>();
        for (Vehicle vehicle : new VehicleSort().sort(vehicles, comparator)) {
            sortedVehicles.add(Map.of(
                "plate", vehicle.getPlateNumber(),
                "type", vehicle.getType()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", manager.getTotalSpaces());
        result.put("available", manager.getAvailableSpaces());
        result.put("spaces", spaces);
        result.put("vehicles", sortedVehicles);
        result.put("history", manager.getHistoryNewestFirst());
        return result;
    }

    private void handleFile(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendJson(exchange, 405, Map.of("message", "Use GET."));
            return;
        }

        String route = exchange.getRequestURI().getPath();
        String file = switch (route) {
            case "/" -> "index.html";
            case "/app.js" -> "app.js";
            case "/style.css" -> "style.css";
            case "/help.html" -> "help.html";
            case "/vendor/vue.global.prod.js" -> "vendor/vue.global.prod.js";
            default -> null;
        };

        if (file == null || !Files.isRegularFile(Path.of("web", file))) {
            sendJson(exchange, 404, Map.of("message", "Page not found."));
            return;
        }

        String type = file.endsWith(".js") ? "text/javascript"
            : file.endsWith(".css") ? "text/css" : "text/html";

        byte[] bytes = Files.readAllBytes(Path.of("web", file));
        exchange.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
        exchange.close();
    }

    private void sendJson(HttpExchange exchange, int status,
                          Object value) throws IOException {
        byte[] bytes = gson.toJson(value).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
        exchange.close();
    }
}
