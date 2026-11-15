import java.util.List;

public class ParkingSearchDemo {
    public static void main(String[] args) {
        ParkingManager manager = new ParkingManager();
        VehicleSearch search = new VehicleSearch();

        manager.parkVehicle(new Vehicle.Car("CAR123"), "1-C-1");
        manager.parkVehicle(new Vehicle.Bus("BUS001"), "2-B-1");

        List<String> car = search.findSpaces(manager.getSpaces(), "car123");
        List<String> bus = search.findSpaces(manager.getSpaces(), "BUS001");
        List<String> missing = search.findSpaces(manager.getSpaces(), "UNKNOWN");

        System.out.println("Car location: " + car);
        System.out.println("Bus location: " + bus);
        System.out.println("Unknown plate: " + missing);

        if (!car.equals(List.of("1-C-1"))) {
            throw new AssertionError("Incorrect car location.");
        }

        if (!bus.equals(List.of(
                "2-B-1", "2-B-2", "2-B-3", "2-B-4", "2-B-5"))) {
            throw new AssertionError("Incorrect bus locations.");
        }

        if (!missing.isEmpty()) {
            throw new AssertionError("An unknown plate returned a location.");
        }

        manager.moveVehicle("CAR123", "3-C-2");
        if (!search.findSpaces(manager.getSpaces(), "CAR123")
                .equals(List.of("3-C-2"))) {
            throw new AssertionError("Search returned an outdated location.");
        }

        System.out.println("Search checks passed.");
    }
}
