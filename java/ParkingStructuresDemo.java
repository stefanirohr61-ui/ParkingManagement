import java.util.List;

public class ParkingStructuresDemo {
    public static void main(String[] args) {
        ParkingManager manager = new ParkingManager();

        ParkingSpace found = manager.getSpaceById(" 1-b-3 ");
        if (found == null || !found.getId().equals("1-B-3")) {
            throw new AssertionError("Hash lookup failed.");
        }
        System.out.println("Hash lookup: " + found.getId());

        if (manager.getSpaceById("9-Z-99") != null) {
            throw new AssertionError("Unknown space was found.");
        }

        List<ParkingSpace> ordered = manager.getSpacesOrdered();
        if (ordered.size() != 60
                || !ordered.get(0).getId().equals("1-A-1")
                || !ordered.get(59).getId().equals("3-C-5")
                || !ordered.get(13).getId().equals("1-B-9")
                || !ordered.get(14).getId().equals("1-B-10")) {
            throw new AssertionError("Incorrect tree order.");
        }

        // Verify that insertion order does not determine traversal order.
        ParkingSpaceTree tree = new ParkingSpaceTree();
        tree.insert(manager.getSpaceById("1-B-10"));
        tree.insert(manager.getSpaceById("1-B-2"));
        tree.insert(manager.getSpaceById("1-B-3"));

        List<ParkingSpace> sample = tree.inOrder();
        if (!sample.get(0).getId().equals("1-B-2")
                || !sample.get(1).getId().equals("1-B-3")
                || !sample.get(2).getId().equals("1-B-10")) {
            throw new AssertionError("Numeric ordering failed.");
        }
        System.out.println("Tree traversal: 60 spaces in order");

        // One bucket forces collisions so we can check the linked entries.
        SpaceHashTable collisionTable = new SpaceHashTable(1);
        collisionTable.put(manager.getSpaceById("1-A-1"));
        collisionTable.put(manager.getSpaceById("1-B-1"));

        if (collisionTable.size() != 2
                || collisionTable.get("1-A-1") == null
                || collisionTable.get("1-B-1") == null
                || collisionTable.get("UNKNOWN") != null) {
            throw new AssertionError("Collision handling failed.");
        }
        System.out.println("Hash collisions handled correctly");

        manager.parkVehicle(new Vehicle.Car("CAR123"), "1-C-1");
        if (manager.getSpaceById("1-C-1").isAvailable()) {
            throw new AssertionError("Index has outdated parking data.");
        }

        manager.removeVehicle("CAR123");
        if (!manager.getSpaceById("1-C-1").isAvailable()) {
            throw new AssertionError("Index has outdated departure data.");
        }

        System.out.println("Data structure checks passed.");
    }
}
