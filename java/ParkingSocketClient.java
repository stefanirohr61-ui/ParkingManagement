import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ParkingSocketClient {
    public static void main(String[] args) throws Exception {
        String[] messages = {
            "PARK|Motorcycle|MOTO01|1-A-1",
            "PARK|Car|CAR123|1-C-1",
            "PARK|Bus|BUS001|2-C-1"
        };

        try (Socket connection = new Socket()) {
            connection.connect(
                new InetSocketAddress("127.0.0.1", 5050), 5000);

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(),
                        StandardCharsets.UTF_8))) {

                for (String message : messages) {
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                    System.out.println("Produced and sent: " + message);
                }

                writer.write("END");
                writer.newLine();
                writer.flush();
            }
        }

        System.out.println("Producer finished.");
    }
}
