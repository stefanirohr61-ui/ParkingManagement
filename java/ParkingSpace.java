public class ParkingSpace {
    public enum SpaceType {
        MOTORCYCLE, COMPACT, LARGE
    }

    private final int level;
    private final String row;
    private final int number;
    private final SpaceType type;
    private Vehicle vehicle;

    public ParkingSpace(int level, String row, int number, SpaceType type) {
        if (level < 1 || number < 1 || row == null
                || row.isBlank() || type == null) {
            throw new IllegalArgumentException("Invalid parking space details.");
        }

        this.level = level;
        this.row = row.trim().toUpperCase();
        this.number = number;
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public String getRow() {
        return row;
    }

    public int getNumber() {
        return number;
    }

    public SpaceType getType() {
        return type;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getId() {
        return level + "-" + row + "-" + number;
    }

    public boolean isAvailable() {
        return vehicle == null;
    }

    // Checks one space. The parking manager checks the full bus allocation.
    public boolean canFit(Vehicle candidate) {
        if (candidate == null || !isAvailable()) {
            return false;
        }

        if (candidate instanceof Vehicle.Motorcycle) {
            return true;
        }

        if (candidate instanceof Vehicle.Car) {
            return type == SpaceType.COMPACT || type == SpaceType.LARGE;
        }

        if (candidate instanceof Vehicle.Bus) {
            return type == SpaceType.LARGE;
        }

        return false;
    }

    // Internal operations used by the parking manager.
    void occupy(Vehicle candidate) {
        if (!canFit(candidate)) {
            throw new IllegalArgumentException("Space unavailable or unsuitable.");
        }
        vehicle = candidate;
    }

    void release() {
        vehicle = null;
    }
}
