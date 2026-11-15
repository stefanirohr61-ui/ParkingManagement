public class ParkingDemo {
    public static void main(String[] args) {
        Vehicle car = new Vehicle.Car("CAR123");
        Vehicle motorcycle = new Vehicle.Motorcycle("MOTO01");

        ParkingSpace space = new ParkingSpace(
            1, "A", 1, ParkingSpace.SpaceType.MOTORCYCLE
        );

        System.out.println("Space: " + space.getId());
        System.out.println("Available: " + space.isAvailable());

        System.out.println("Can a car park here? " + space.canFit(car));
        System.out.println("Can a motorcycle park here? "
            + space.canFit(motorcycle));

        space.occupy(motorcycle);
        System.out.println("Parked vehicle: "
            + space.getVehicle().getPlateNumber());
        System.out.println("Available after parking: " + space.isAvailable());

        space.release();
        System.out.println("Available after departure: " + space.isAvailable());
    }
}
