# Sorting algorithm comparison

| Algorithm | Average time | Worst time | Limitation |
|---|---|---|---|
| Bubble sort | O(n^2) | O(n^2) | Many comparisons and swaps. |
| Quick sort | O(n log n) | O(n^2) | Performance depends on pivot selection. |
| Merge sort | O(n log n) | O(n log n) | Requires O(n) additional memory. |

## Selected algorithm

Merge sort provides predictable performance and supports a comparator.
Its additional memory is acceptable for this small parking application.

The comparator sorts vehicles alphabetically by type:
Bus, Car and Motorcycle. Plate number is used to break ties.

Each vehicle appears once in the list, including buses.
Sorting returns a new list and does not change parking locations.
