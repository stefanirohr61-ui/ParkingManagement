public class ParkingLayoutDemo {
    public static void main(String[] args) {
        ParkingManager manager = new ParkingManager();

        System.out.println("Total spaces: " + manager.getTotalSpaces());
        System.out.println("Available spaces: " + manager.getAvailableSpaces());

        System.out.println("Motorcycle spaces: "
            + manager.countSpacesByType(ParkingSpace.SpaceType.MOTORCYCLE));
        System.out.println("Compact spaces: "
            + manager.countSpacesByType(ParkingSpace.SpaceType.COMPACT));
        System.out.println("Large spaces: "
            + manager.countSpacesByType(ParkingSpace.SpaceType.LARGE));
    }
}
