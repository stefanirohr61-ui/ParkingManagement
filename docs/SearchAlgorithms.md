# Search algorithm comparison

| Algorithm | Search time | Benefit | Limitation |
|---|---|---|---|
| Linear search | O(n) | Works with unsorted data and is simple to implement. | May check every record. |
| Binary search | O(log n) | Uses few comparisons on sorted data. | Requires records sorted by the search key. |
| Jump search | O(sqrt(n)) | Skips blocks when searching sorted data. | Requires sorted data and a final linear scan. |

## Selected algorithm

Linear search is suitable because the car park has only 60 spaces.
Vehicles can enter, leave and move without keeping records sorted by plate.
The search checks each space and returns all spaces occupied by the vehicle.
This also supports buses, which occupy five spaces.

The time complexity is O(n). Binary and jump search would also require
preparing and maintaining a separate collection sorted by plate.
