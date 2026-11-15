public class ParkingMoveDemo {
    public static void main(String[] args) {
        ParkingManager manager = new ParkingManager();

        manager.parkVehicle(new Vehicle.Bus("BUS001"), "1-B-1");

        // Move from spaces 1-5 to spaces 3-7, with partial overlap.
        manager.moveVehicle("BUS001", "1-B-3");
        System.out.println("Available after moving: "
            + manager.getAvailableSpaces());

        // Level 3 has no large spaces, so this move must fail.
        try {
            manager.moveVehicle("BUS001", "3-B-1");
            throw new AssertionError("An unsuitable destination was accepted.");
        } catch (IllegalArgumentException error) {
            System.out.println("Blocked: " + error.getMessage());
        }

        int busSpaces = 0;
        for (ParkingSpace space : manager.getSpaces()) {
            if (!space.isAvailable()) {
                if (!space.getVehicle().getPlateNumber().equals("BUS001")
                        || space.getLevel() != 1
                        || !space.getRow().equals("B")
                        || space.getNumber() < 3
                        || space.getNumber() > 7) {
                    throw new AssertionError("Incorrect vehicle location.");
                }
                busSpaces++;
            }
        }

        if (busSpaces != 5 || manager.getAvailableSpaces() != 55) {
            throw new AssertionError("Incorrect space count after moving.");
        }

        System.out.println("Bus remains in spaces 1-B-3 to 1-B-7.");
        System.out.println("Movement checks passed.");
    }
}
