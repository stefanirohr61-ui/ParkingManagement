import java.util.ArrayList;
import java.util.List;

public class ParkingSpaceTree {
    private static class Node {
        private final ParkingSpace space;
        private Node left;
        private Node right;

        private Node(ParkingSpace space) {
            this.space = space;
        }
    }

    private Node root;

    public void insert(ParkingSpace space) {
        if (space == null) {
            throw new IllegalArgumentException("Space is required.");
        }
        root = insert(root, space);
    }

    private Node insert(Node node, ParkingSpace space) {
        if (node == null) {
            return new Node(space);
        }

        int order = compare(space, node.space);
        if (order < 0) {
            node.left = insert(node.left, space);
        } else if (order > 0) {
            node.right = insert(node.right, space);
        } else {
            throw new IllegalArgumentException("Duplicate space.");
        }

        return node;
    }

    private int compare(ParkingSpace first, ParkingSpace second) {
        int order = Integer.compare(first.getLevel(), second.getLevel());
        if (order == 0) {
            order = first.getRow().compareTo(second.getRow());
        }
        if (order == 0) {
            order = Integer.compare(first.getNumber(), second.getNumber());
        }
        return order;
    }

    public List<ParkingSpace> inOrder() {
        List<ParkingSpace> result = new ArrayList<>();
        traverse(root, result);
        return result;
    }

    private void traverse(Node node, List<ParkingSpace> result) {
        if (node != null) {
            traverse(node.left, result);
            result.add(node.space);
            traverse(node.right, result);
        }
    }
}
