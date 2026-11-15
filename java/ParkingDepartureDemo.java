public class ParkingDepartureDemo {
    public static void main(String[] args) {
        ParkingManager manager = new ParkingManager();

        manager.parkVehicle(new Vehicle.Bus("BUS001"), "1-B-1");
        System.out.println("Available after parking: "
            + manager.getAvailableSpaces());

        int released = manager.removeVehicle("bus001");
        System.out.println("Spaces released: " + released);
        System.out.println("Available after departure: "
            + manager.getAvailableSpaces());

        if (released != 5 || manager.getAvailableSpaces() != 60) {
            throw new AssertionError("Bus spaces were not released correctly.");
        }

        try {
            manager.removeVehicle("UNKNOWN");
            throw new AssertionError("An unknown plate was accepted.");
        } catch (IllegalArgumentException error) {
            System.out.println("Blocked: " + error.getMessage());
        }

        if (manager.getAvailableSpaces() != 60) {
            throw new AssertionError("The blocked action changed the count.");
        }
    }
}
