public abstract class Vehicle {
    private final String plateNumber;

    protected Vehicle(String plateNumber) {
        if (plateNumber == null || plateNumber.isBlank()) {
            throw new IllegalArgumentException("Plate number is required.");
        }
        this.plateNumber = plateNumber.trim().toUpperCase();
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public abstract String getType();

    public abstract int getSpacesNeeded();

    public static class Motorcycle extends Vehicle {
        public Motorcycle(String plateNumber) {
            super(plateNumber);
        }

        @Override
        public String getType() {
            return "Motorcycle";
        }

        @Override
        public int getSpacesNeeded() {
            return 1;
        }
    }

    public static class Car extends Vehicle {
        public Car(String plateNumber) {
            super(plateNumber);
        }

        @Override
        public String getType() {
            return "Car";
        }

        @Override
        public int getSpacesNeeded() {
            return 1;
        }
    }

    public static class Bus extends Vehicle {
        public Bus(String plateNumber) {
            super(plateNumber);
        }

        @Override
        public String getType() {
            return "Bus";
        }

        @Override
        public int getSpacesNeeded() {
            return 5;
        }
    }
}