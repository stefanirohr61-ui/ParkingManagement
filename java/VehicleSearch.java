import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleSearch {

    public List<String> findSpaces(List<ParkingSpace> spaces,
                                   String plateNumber) {
        if (spaces == null || plateNumber == null || plateNumber.isBlank()) {
            throw new IllegalArgumentException("Spaces and plate are required.");
        }

        String plate = plateNumber.trim().toUpperCase(Locale.ROOT);
        List<String> locations = new ArrayList<>();

        // Linear search: check every space and collect matching locations.
        for (ParkingSpace space : spaces) {
            if (!space.isAvailable()
                    && space.getVehicle().getPlateNumber().equals(plate)) {
                locations.add(space.getId());
            }
        }

        return locations;
    }
}
