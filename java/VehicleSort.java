import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VehicleSort {

    public List<Vehicle> sort(List<Vehicle> vehicles,
                              Comparator<Vehicle> comparator) {
        if (vehicles == null || comparator == null) {
            throw new IllegalArgumentException("Vehicles and comparator are required.");
        }

        List<Vehicle> copy = new ArrayList<>(vehicles);
        mergeSort(copy, comparator);
        return copy;
    }

    private void mergeSort(List<Vehicle> vehicles,
                           Comparator<Vehicle> comparator) {
        if (vehicles.size() < 2) {
            return;
        }

        int middle = vehicles.size() / 2;
        List<Vehicle> left = new ArrayList<>(vehicles.subList(0, middle));
        List<Vehicle> right = new ArrayList<>(
            vehicles.subList(middle, vehicles.size()));

        mergeSort(left, comparator);
        mergeSort(right, comparator);

        int i = 0;
        int j = 0;
        int position = 0;

        // Select from the left on a tie to preserve a stable order.
        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                vehicles.set(position++, left.get(i++));
            } else {
                vehicles.set(position++, right.get(j++));
            }
        }

        while (i < left.size()) {
            vehicles.set(position++, left.get(i++));
        }

        while (j < right.size()) {
            vehicles.set(position++, right.get(j++));
        }
    }
}
