import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParkingManager {
    private final List<ParkingSpace> spaces = new ArrayList<>();

    public ParkingManager() {
        // Level 1: 5 motorcycle, 10 large and 5 compact spaces.
        addRow(1, "A", 5, ParkingSpace.SpaceType.MOTORCYCLE);
        addRow(1, "B", 10, ParkingSpace.SpaceType.LARGE);
        addRow(1, "C", 5, ParkingSpace.SpaceType.COMPACT);

        // Level 2: 5 motorcycle, 5 large and 10 compact spaces.
        addRow(2, "A", 5, ParkingSpace.SpaceType.MOTORCYCLE);
        addRow(2, "B", 5, ParkingSpace.SpaceType.LARGE);
        addRow(2, "C", 10, ParkingSpace.SpaceType.COMPACT);

        // Level 3: 5 motorcycle and 15 compact spaces.
        addRow(3, "A", 5, ParkingSpace.SpaceType.MOTORCYCLE);
        addRow(3, "B", 10, ParkingSpace.SpaceType.COMPACT);
        addRow(3, "C", 5, ParkingSpace.SpaceType.COMPACT);
    }

    private void addRow(int level, String row, int count,
                        ParkingSpace.SpaceType type) {
        for (int number = 1; number <= count; number++) {
            spaces.add(new ParkingSpace(level, row, number, type));
        }
    }

    public List<ParkingSpace> getSpaces() {
        // Other classes can read the layout without adding or removing spaces.
        return Collections.unmodifiableList(spaces);
    }

    public int getTotalSpaces() {
        return spaces.size();
    }

    public int getAvailableSpaces() {
        int available = 0;
        for (ParkingSpace space : spaces) {
            if (space.isAvailable()) {
                available++;
            }
        }
        return available;
    }

    public int countSpacesByType(ParkingSpace.SpaceType type) {
        int count = 0;
        for (ParkingSpace space : spaces) {
            if (space.getType() == type) {
                count++;
            }
        }
        return count;
    }

    public void parkVehicle(Vehicle vehicle, String startSpaceId) {
        if (vehicle == null || startSpaceId == null) {
            throw new IllegalArgumentException("Vehicle and space are required.");
        }

        // Prevent the same plate from being registered twice.
        for (ParkingSpace space : spaces) {
            if (!space.isAvailable()
                    && space.getVehicle().getPlateNumber()
                        .equals(vehicle.getPlateNumber())) {
                throw new IllegalArgumentException("Vehicle is already parked.");
            }
        }

        ParkingSpace start = null;
        for (ParkingSpace space : spaces) {
            if (space.getId().equalsIgnoreCase(startSpaceId.trim())) {
                start = space;
                break;
            }
        }

        if (start == null) {
            throw new IllegalArgumentException("Parking space not found.");
        }

        List<ParkingSpace> allocation = new ArrayList<>();

        // Check the full allocation before changing any space.
        for (int offset = 0; offset < vehicle.getSpacesNeeded(); offset++) {
            ParkingSpace target = null;

            for (ParkingSpace space : spaces) {
                if (space.getLevel() == start.getLevel()
                        && space.getRow().equals(start.getRow())
                        && space.getNumber() == start.getNumber() + offset) {
                    target = space;
                    break;
                }
            }

            if (target == null || !target.canFit(vehicle)) {
                throw new IllegalArgumentException(
                    "Not enough consecutive suitable spaces.");
            }

            allocation.add(target);
        }

        for (ParkingSpace space : allocation) {
            space.occupy(vehicle);
        }
        recordEvent("PARK " + vehicle.getPlateNumber() + " at " + start.getId());
    }

    public int removeVehicle(String plateNumber) {
        if (plateNumber == null || plateNumber.isBlank()) {
            throw new IllegalArgumentException("Plate number is required.");
        }

        String plate = plateNumber.trim().toUpperCase();
        List<ParkingSpace> allocation = new ArrayList<>();

        for (ParkingSpace space : spaces) {
            if (!space.isAvailable()
                    && space.getVehicle().getPlateNumber().equals(plate)) {
                allocation.add(space);
            }
        }

        if (allocation.isEmpty()) {
            throw new IllegalArgumentException("Vehicle not found.");
        }

        // Release every space occupied by this vehicle.
        for (ParkingSpace space : allocation) {
            space.release();
        }

        recordEvent("DEPART " + plate);
        return allocation.size();
    }

    public void moveVehicle(String plateNumber, String destinationId) {
        if (plateNumber == null || plateNumber.isBlank()
                || destinationId == null || destinationId.isBlank()) {
            throw new IllegalArgumentException("Plate and destination are required.");
        }

        String plate = plateNumber.trim().toUpperCase();
        Vehicle vehicle = null;
        ParkingSpace destination = null;
        List<ParkingSpace> currentSpaces = new ArrayList<>();

        for (ParkingSpace space : spaces) {
            if (!space.isAvailable()
                    && space.getVehicle().getPlateNumber().equals(plate)) {
                vehicle = space.getVehicle();
                currentSpaces.add(space);
            }

            if (space.getId().equalsIgnoreCase(destinationId.trim())) {
                destination = space;
            }
        }

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }

        if (destination == null) {
            throw new IllegalArgumentException("Destination not found.");
        }

        List<ParkingSpace> targetSpaces = new ArrayList<>();

        // Validate the complete destination before releasing current spaces.
        for (int offset = 0; offset < vehicle.getSpacesNeeded(); offset++) {
            ParkingSpace target = null;

            for (ParkingSpace space : spaces) {
                if (space.getLevel() == destination.getLevel()
                        && space.getRow().equals(destination.getRow())
                        && space.getNumber() == destination.getNumber() + offset) {
                    target = space;
                    break;
                }
            }

            // A bus may move into some of its own current spaces.
            if (target == null
                    || (target.getVehicle() != vehicle && !target.canFit(vehicle))) {
                throw new IllegalArgumentException(
                    "Destination unavailable or unsuitable.");
            }

            targetSpaces.add(target);
        }

        for (ParkingSpace space : currentSpaces) {
            space.release();
        }

        for (ParkingSpace space : targetSpaces) {
            space.occupy(vehicle);
        }
        recordEvent("MOVE " + plate + " to " + destination.getId());
    }

    private final DoublyLinkedList<String> history = new DoublyLinkedList<>();

    private void recordEvent(String event) {
        history.addLast(event);

        // Keep the latest 100 successful actions.
        if (history.size() > 100) {
            history.removeFirst();
        }
    }

    public List<String> getHistory() {
        return history.forward();
    }

    public List<String> getHistoryNewestFirst() {
        return history.backward();
    }
}




