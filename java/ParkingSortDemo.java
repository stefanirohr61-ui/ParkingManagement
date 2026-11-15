import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ParkingSortDemo {
    public static void main(String[] args) {
        ParkingManager manager = new ParkingManager();

        manager.parkVehicle(new Vehicle.Motorcycle("MOTO01"), "1-A-1");
        manager.parkVehicle(new Vehicle.Car("CAR200"), "1-C-1");
        manager.parkVehicle(new Vehicle.Bus("BUS001"), "1-B-1");
        manager.parkVehicle(new Vehicle.Car("CAR100"), "1-C-2");

        // Include each vehicle once, even when it occupies five spaces.
        List<Vehicle> vehicles = new ArrayList<>();
        for (ParkingSpace space : manager.getSpaces()) {
            if (!space.isAvailable() && !vehicles.contains(space.getVehicle())) {
                vehicles.add(space.getVehicle());
            }
        }

        Comparator<Vehicle> comparator = Comparator
            .comparing(Vehicle::getType)
            .thenComparing(Vehicle::getPlateNumber);

        VehicleSort sorter = new VehicleSort();
        List<Vehicle> sorted = sorter.sort(vehicles, comparator);

        for (Vehicle vehicle : sorted) {
            System.out.println(vehicle.getType() + ": "
                + vehicle.getPlateNumber());
        }

        List<String> plates = new ArrayList<>();
        for (Vehicle vehicle : sorted) {
            plates.add(vehicle.getPlateNumber());
        }

        if (!plates.equals(List.of("BUS001", "CAR100", "CAR200", "MOTO01"))) {
            throw new AssertionError("Incorrect sorting order.");
        }

        if (manager.getAvailableSpaces() != 52) {
            throw new AssertionError("Sorting changed parking records.");
        }

        if (!sorter.sort(List.of(), comparator).isEmpty()) {
            throw new AssertionError("An empty list was not handled correctly.");
        }

        System.out.println("Sorting checks passed.");
    }
}
