import java.util.List;

public class LinkedListDemo {
    public static void main(String[] args) {
        DoublyLinkedList<String> history = new DoublyLinkedList<>();

        history.addLast("Vehicle parked");
        history.addLast("Vehicle moved");
        history.addLast("Vehicle departed");

        System.out.println("Oldest first: " + history.forward());
        System.out.println("Newest first: " + history.backward());

        if (!history.forward().equals(List.of(
                "Vehicle parked", "Vehicle moved", "Vehicle departed"))) {
            throw new AssertionError("Incorrect forward links.");
        }

        if (!history.backward().equals(List.of(
                "Vehicle departed", "Vehicle moved", "Vehicle parked"))) {
            throw new AssertionError("Incorrect backward links.");
        }

        history.removeFirst();
        if (history.size() != 2
                || !history.backward().equals(List.of(
                    "Vehicle departed", "Vehicle moved"))) {
            throw new AssertionError("Incorrect links after removal.");
        }

        history.removeFirst();
        history.removeFirst();

        if (history.size() != 0
                || !history.forward().isEmpty()
                || !history.backward().isEmpty()) {
            throw new AssertionError("List did not become empty.");
        }

        // Check that the list can be reused after becoming empty.
        history.addLast("New event");
        if (!history.backward().equals(List.of("New event"))) {
            throw new AssertionError("List could not be reused.");
        }

        System.out.println("Linked list checks passed.");
    }
}
