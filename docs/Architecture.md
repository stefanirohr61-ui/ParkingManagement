# Architecture and external dependencies

## Application structure

The application separates the interface, request handling and parking rules.

- View: HTML, CSS, SVG graphics and Vue.
- Controller: ParkingWebServer receives HTTP requests and returns JSON.
- Model: ParkingManager, Vehicle, ParkingSpace and the data structures.
- C++ implementation: demonstrates the same parking rules in a console program.

Java and C++ use the same layout and vehicle restrictions.
Each demonstration has its own parking records in memory.
Records are not shared automatically between the web interface and the demos.

## Framework: Vue 3.5.13

Vue manages the interface state and updates the map, counters and lists.
Its reactive approach supports a clear separation between displayed data
and interface behaviour. It is suitable for this small application because
it can run locally without an additional frontend build process.

## Third-party library: Gson 2.11.0

Gson converts Java data into JSON and reads JSON requests from the interface.
This avoids manually building and parsing JSON text.

## Data structures and algorithms

- Doubly linked list: stores the latest 100 successful parking actions.
- Binary search tree: lists spaces by level, row and number.
- Custom hash table: finds a parking space by its identifier.
- Linear search: locates all spaces occupied by a vehicle.
- Merge sort: sorts vehicles using a type and plate comparator.

The binary tree is not self-balancing. Its worst-case operation time is O(n).
Hash lookup is O(1) on average and O(n) in the worst case.

## References

Vue documentation: https://vuejs.org/guide/introduction.html
Gson documentation: https://github.com/google/gson
