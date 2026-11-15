import java.util.ArrayList;
import java.util.List;

public class DoublyLinkedList<T> {
    private static class Node<T> {
        private final T value;
        private Node<T> previous;
        private Node<T> next;

        private Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void addLast(T value) {
        Node<T> node = new Node<>(value);
        node.previous = tail;

        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }

        tail = node;
        size++;
    }

    public T removeFirst() {
        if (head == null) {
            throw new IllegalStateException("List is empty.");
        }

        T value = head.value;
        head = head.next;

        if (head == null) {
            tail = null;
        } else {
            head.previous = null;
        }

        size--;
        return value;
    }

    public int size() {
        return size;
    }

    public List<T> forward() {
        List<T> values = new ArrayList<>();
        Node<T> current = head;

        while (current != null) {
            values.add(current.value);
            current = current.next;
        }

        return values;
    }

    public List<T> backward() {
        List<T> values = new ArrayList<>();
        Node<T> current = tail;

        while (current != null) {
            values.add(current.value);
            current = current.previous;
        }

        return values;
    }
}
