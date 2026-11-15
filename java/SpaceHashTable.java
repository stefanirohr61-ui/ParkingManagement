import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpaceHashTable {
    private static class Entry {
        private final String key;
        private final ParkingSpace value;
        private Entry next;

        private Entry(String key, ParkingSpace value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private final List<Entry> buckets;
    private int size;

    public SpaceHashTable(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        buckets = new ArrayList<>(Collections.nCopies(capacity, null));
    }

    private int hash(String key) {
        int result = 0;
        for (int i = 0; i < key.length(); i++) {
            result = (int) (((long) result * 31 + key.charAt(i))
                % buckets.size());
        }
        return result;
    }

    public void put(ParkingSpace space) {
        if (space == null) {
            throw new IllegalArgumentException("Space is required.");
        }

        String key = space.getId();
        int bucket = hash(key);

        for (Entry entry = buckets.get(bucket);
                entry != null; entry = entry.next) {
            if (entry.key.equals(key)) {
                throw new IllegalArgumentException("Duplicate space.");
            }
        }

        buckets.set(bucket, new Entry(key, space, buckets.get(bucket)));
        size++;
    }

    public ParkingSpace get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key is required.");
        }

        for (Entry entry = buckets.get(hash(key));
                entry != null; entry = entry.next) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }

    public int size() {
        return size;
    }
}
