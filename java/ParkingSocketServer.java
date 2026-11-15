import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ParkingSocketServer {
    public static void main(String[] args) throws Exception {
        ParkingManager manager = new ParkingManager();
        int consumed = 0;

        try (ServerSocket server = new ServerSocket(
                5050, 1, InetAddress.getByName("127.0.0.1"))) {
            System.out.println("Consumer waiting on port 5050...");

            try (Socket connection = server.accept();
                 BufferedReader reader = new BufferedReader(
                     new InputStreamReader(connection.getInputStream(),
                         StandardCharsets.UTF_8))) {

                connection.setSoTimeout(30000);
                boolean finished = false;
                String message;

                while ((message = reader.readLine()) != null) {
                    if (message.equals("END")) {
                        finished = true;
                        break;
                    }

                    System.out.println("Received: " + message);
                    String[] parts = message.split("\\|", -1);

                    if (parts.length != 4 || !parts[0].equals("PARK")) {
                        throw new IllegalArgumentException("Invalid message.");
                    }

                    Vehicle vehicle = switch (parts[1]) {
                        case "Motorcycle" -> new Vehicle.Motorcycle(parts[2]);
                        case "Car" -> new Vehicle.Car(parts[2]);
                        case "Bus" -> new Vehicle.Bus(parts[2]);
                        default -> throw new IllegalArgumentException(
                            "Unknown vehicle type.");
                    };

                    manager.parkVehicle(vehicle, parts[3]);
                    consumed++;
                    System.out.println("Consumed: vehicle parked.");
                }

                if (!finished) {
                    throw new IllegalStateException("Missing END message.");
                }
            }
        }

        System.out.println("Messages consumed: " + consumed);
        System.out.println("Available spaces: " + manager.getAvailableSpaces());

        if (consumed != 3 || manager.getAvailableSpaces() != 53) {
            throw new AssertionError("Unexpected consumer result.");
        }

        System.out.println("Socket message-passing checks passed.");
    }
}
