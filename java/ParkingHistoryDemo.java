import java.util.List;

public class ParkingHistoryDemo {
    public static void main(String[] args) {
        ParkingManager manager = new ParkingManager();

        manager.parkVehicle(new Vehicle.Car("CAR123"), "1-C-1");
        manager.moveVehicle("CAR123", "3-C-2");

        // A blocked move must not create a successful action record.
        try {
            manager.moveVehicle("CAR123", "1-A-1");
            throw new AssertionError("A car was accepted in a motorcycle space.");
        } catch (IllegalArgumentException expected) {
            // Expected: the car remains in its current space.
        }

        manager.removeVehicle("CAR123");

        List<String> expected = List.of(
            "PARK CAR123 at 1-C-1",
            "MOVE CAR123 to 3-C-2",
            "DEPART CAR123"
        );

        if (!manager.getHistory().equals(expected)) {
            throw new AssertionError("Incorrect parking history.");
        }

        if (!manager.getHistoryNewestFirst().equals(List.of(
                expected.get(2), expected.get(1), expected.get(0)))) {
            throw new AssertionError("Incorrect reverse history.");
        }

        System.out.println("Parking history:");
        for (String event : manager.getHistory()) {
            System.out.println(event);
        }

        System.out.println("History checks passed.");
    }
}
