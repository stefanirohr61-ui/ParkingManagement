public class ParkingAllocationDemo {
    public static void main(String[] args) {
        ParkingManager manager = new ParkingManager();

        manager.parkVehicle(new Vehicle.Motorcycle("MOTO01"), "1-A-1");
        manager.parkVehicle(new Vehicle.Car("CAR123"), "1-C-1");
        manager.parkVehicle(new Vehicle.Bus("BUS001"), "1-B-1");

        System.out.println("Available after parking: "
            + manager.getAvailableSpaces());

        // This allocation must fail without changing the existing records.
        try {
            manager.parkVehicle(new Vehicle.Bus("BUS002"), "1-B-4");
            throw new AssertionError("An overlapping bus was accepted.");
        } catch (IllegalArgumentException error) {
            System.out.println("Blocked: " + error.getMessage());
        }

        System.out.println("Available after blocked attempt: "
            + manager.getAvailableSpaces());

        if (manager.getAvailableSpaces() != 53) {
            throw new AssertionError("Incorrect available space count.");
        }
    }
}
