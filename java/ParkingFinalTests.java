import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ParkingFinalTests {
    private static final List<String> results = new ArrayList<>();
    private static int passed;
    private static int failed;

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void blocked(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Invalid action was accepted.");
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            results.add(name + ",PASS," + Instant.now());
            System.out.println("PASS: " + name);
        } catch (AssertionError | RuntimeException error) {
            failed++;
            results.add(name + ",FAIL," + Instant.now());
            System.out.println("FAIL: " + name + " - " + error.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        results.add("Test,Result,TimeUTC");

        run("Layout and capacity", () -> {
            ParkingManager manager = new ParkingManager();
            check(manager.getTotalSpaces() == 60, "Incorrect total.");
            check(manager.getAvailableSpaces() == 60, "Incorrect availability.");
            check(manager.countSpacesByType(
                ParkingSpace.SpaceType.MOTORCYCLE) == 15, "Motorcycle count.");
            check(manager.countSpacesByType(
                ParkingSpace.SpaceType.COMPACT) == 30, "Compact count.");
            check(manager.countSpacesByType(
                ParkingSpace.SpaceType.LARGE) == 15, "Large count.");

            for (int i = 1; i <= 5; i++) {
                check(manager.getSpaceById("2-C-" + i).getType()
                    == ParkingSpace.SpaceType.LARGE, "Level 2 layout.");
            }
        });

        run("Input validation and duplicate plates", () -> {
            ParkingManager manager = new ParkingManager();
            blocked(() -> new Vehicle.Car(" "));
            blocked(() -> new Vehicle.Car(null));
            manager.parkVehicle(new Vehicle.Car(" car123 "), "1-C-1");

            blocked(() -> manager.parkVehicle(
                new Vehicle.Car("CAR123"), "1-C-2"));
            blocked(() -> manager.parkVehicle(
                new Vehicle.Car("CAR200"), "1-A-1"));
            blocked(() -> manager.parkVehicle(
                new Vehicle.Car("CAR300"), "9-Z-99"));

            check(manager.getAvailableSpaces() == 59, "Records changed.");
            check(manager.getHistory().size() == 1, "Invalid history entry.");
        });

        run("Bus fragmentation and row boundary", () -> {
            ParkingManager manager = new ParkingManager();

            // Five large spaces remain free, but they are not consecutive.
            for (int number = 2; number <= 10; number += 2) {
                manager.parkVehicle(new Vehicle.Car("CAR" + number),
                    "1-B-" + number);
            }

            blocked(() -> manager.parkVehicle(
                new Vehicle.Bus("BUS001"), "1-B-1"));
            blocked(() -> manager.parkVehicle(
                new Vehicle.Bus("BUS002"), "2-C-2"));

            check(manager.getAvailableSpaces() == 55, "Partial bus allocation.");
            check(new VehicleSearch().findSpaces(
                manager.getSpaces(), "BUS001").isEmpty(), "Bus partly parked.");

            manager.parkVehicle(new Vehicle.Bus("BUS003"), "2-C-1");
            check(manager.getAvailableSpaces() == 50, "Valid bus allocation.");
            check(manager.removeVehicle("BUS003") == 5, "Bus departure.");
            check(manager.getAvailableSpaces() == 55, "Bus spaces not released.");
        });

        run("Failed movement preserves location", () -> {
            ParkingManager manager = new ParkingManager();
            manager.parkVehicle(new Vehicle.Car("CAR123"), "1-C-1");
            manager.parkVehicle(new Vehicle.Car("CAR200"), "1-C-2");

            blocked(() -> manager.moveVehicle("CAR123", "1-A-1"));
            blocked(() -> manager.moveVehicle("CAR123", "1-C-2"));
            blocked(() -> manager.moveVehicle("UNKNOWN", "1-C-3"));

            check(new VehicleSearch().findSpaces(
                manager.getSpaces(), "CAR123").equals(List.of("1-C-1")),
                "Original location changed.");
            check(manager.getAvailableSpaces() == 58, "Count changed.");
            check(manager.getHistory().size() == 2, "Failed move recorded.");
        });

        run("Full capacity and response time", () -> {
            ParkingManager manager = new ParkingManager();
            long longestPark = 0;
            int number = 0;

            for (ParkingSpace space : manager.getSpaces()) {
                Vehicle vehicle = new Vehicle.Motorcycle("FULL" + number++);
                long start = System.nanoTime();
                manager.parkVehicle(vehicle, space.getId());
                longestPark = Math.max(longestPark, System.nanoTime() - start);
            }

            check(manager.getAvailableSpaces() == 0, "Not fully occupied.");
            blocked(() -> manager.parkVehicle(
                new Vehicle.Motorcycle("EXTRA"), "1-A-1"));

            long start = System.nanoTime();
            List<String> location = new VehicleSearch().findSpaces(
                manager.getSpaces(), "FULL59");
            long searchTime = System.nanoTime() - start;
            check(location.equals(List.of("3-C-5")), "Full capacity search.");

            start = System.nanoTime();
            blocked(() -> manager.moveVehicle("FULL0", "3-C-5"));
            long blockedMoveTime = System.nanoTime() - start;
            check(manager.getAvailableSpaces() == 0, "Blocked move changed count.");

            manager.removeVehicle("FULL59");
            start = System.nanoTime();
            manager.moveVehicle("FULL0", "3-C-5");
            long moveTime = System.nanoTime() - start;

            check(new VehicleSearch().findSpaces(
                manager.getSpaces(), "FULL0").equals(List.of("3-C-5")),
                "Movement location is incorrect.");
            check(manager.getAvailableSpaces() == 1, "Movement count is incorrect.");

            long limit = 2_000_000_000L;
            check(longestPark < limit, "Parking exceeded two seconds.");
            check(searchTime < limit, "Search exceeded two seconds.");
            check(blockedMoveTime < limit, "Blocked move exceeded two seconds.");
            check(moveTime < limit, "Movement exceeded two seconds.");

            System.out.printf(
                "Measured milliseconds: park=%.3f search=%.3f blockedMove=%.3f move=%.3f%n",
                longestPark / 1e6, searchTime / 1e6,
                blockedMoveTime / 1e6, moveTime / 1e6);
        });

        run("History limit and reverse traversal", () -> {
            ParkingManager manager = new ParkingManager();

            for (int i = 0; i < 60; i++) {
                manager.parkVehicle(new Vehicle.Car("H" + i), "1-C-1");
                manager.removeVehicle("H" + i);
            }

            List<String> history = manager.getHistory();
            check(history.size() == 100, "History limit failed.");
            check(history.get(0).equals("PARK H10 at 1-C-1"),
                "Incorrect oldest event.");
            check(history.get(99).equals("DEPART H59"),
                "Incorrect newest event.");

            List<String> reverse = manager.getHistoryNewestFirst();
            for (int i = 0; i < history.size(); i++) {
                check(reverse.get(i).equals(history.get(99 - i)),
                    "Reverse links are incorrect.");
            }
        });

        Path folder = Path.of("tests", "results");
        Files.createDirectories(folder);
        Files.write(folder.resolve("java-tests.csv"), results);

        System.out.println();
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Results saved to tests/results/java-tests.csv");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
