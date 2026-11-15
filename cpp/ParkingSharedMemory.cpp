#define NOMINMAX
#include <windows.h>
#define PARKING_NO_MAIN
#include "ParkingManagement.cpp"

// Only fixed-size data is stored in shared memory, never process pointers.
struct ParkingRequest {
    char type[16];
    char plate[32];
    char destination[16];
    int finished;
};

struct HandleCloser {
    void operator()(void* handle) const {
        if (handle) {
            CloseHandle(handle);
        }
    }
};

struct ViewCloser {
    void operator()(ParkingRequest* view) const {
        if (view) {
            UnmapViewOfFile(view);
        }
    }
};

using Handle = std::unique_ptr<void, HandleCloser>;
using SharedView = std::unique_ptr<ParkingRequest, ViewCloser>;

const char* MEMORY_NAME = "Local\\ICTPRG532_ParkingMemory";
const char* EMPTY_NAME = "Local\\ICTPRG532_ParkingEmpty";
const char* FULL_NAME = "Local\\ICTPRG532_ParkingFull";

void waitFor(HANDLE semaphore) {
    DWORD result = WaitForSingleObject(semaphore, 120000);
    if (result != WAIT_OBJECT_0) {
        throw std::runtime_error(
            "Wait failed or timed out. Start both processes again.");
    }
}

void signal(HANDLE semaphore) {
    if (!ReleaseSemaphore(semaphore, 1, nullptr)) {
        throw std::runtime_error("Could not release semaphore.");
    }
}

Handle createSemaphore(const char* name, LONG initial) {
    Handle handle(CreateSemaphoreA(nullptr, initial, 1, name));
    const DWORD error = GetLastError();

    if (!handle || error == ERROR_ALREADY_EXISTS) {
        throw std::runtime_error(
            "Cannot create semaphore. Close previous demo processes.");
    }
    return handle;
}

void produce() {
    Handle mapping(CreateFileMappingA(
        INVALID_HANDLE_VALUE, nullptr, PAGE_READWRITE,
        0, sizeof(ParkingRequest), MEMORY_NAME));

    const DWORD error = GetLastError();
    if (!mapping || error == ERROR_ALREADY_EXISTS) {
        throw std::runtime_error(
            "Cannot create memory. Close previous demo processes.");
    }

    Handle empty = createSemaphore(EMPTY_NAME, 1);
    Handle full = createSemaphore(FULL_NAME, 0);

    SharedView slot(static_cast<ParkingRequest*>(
        MapViewOfFile(mapping.get(), FILE_MAP_ALL_ACCESS,
            0, 0, sizeof(ParkingRequest))));

    if (!slot) {
        throw std::runtime_error("Could not map shared memory.");
    }

    const std::vector<ParkingRequest> requests = {
        {"Motorcycle", "MOTO01", "1-A-1", 0},
        {"Car", "CAR123", "1-C-1", 0},
        {"Bus", "BUS001", "2-C-1", 0},
        {"", "", "", 1}
    };

    std::cout << "Producer ready. Start the consumer in another terminal."
              << std::endl;

    for (const auto& request : requests) {
        waitFor(empty.get());
        *slot = request;
        signal(full.get());

        if (!request.finished) {
            std::cout << "Produced: " << request.plate << " -> "
                      << request.destination << std::endl;
        }
    }

    // Keep the mapping alive until the consumer reads the final marker.
    waitFor(empty.get());
    std::cout << "Shared-memory producer finished." << std::endl;
}

void consume() {
    Handle mapping(OpenFileMappingA(
        FILE_MAP_ALL_ACCESS, FALSE, MEMORY_NAME));

    if (!mapping) {
        throw std::runtime_error("Start the producer first.");
    }

    Handle empty(OpenSemaphoreA(
        SYNCHRONIZE | SEMAPHORE_MODIFY_STATE, FALSE, EMPTY_NAME));
    Handle full(OpenSemaphoreA(
        SYNCHRONIZE | SEMAPHORE_MODIFY_STATE, FALSE, FULL_NAME));

    if (!empty || !full) {
        throw std::runtime_error("Could not open semaphores.");
    }

    SharedView slot(static_cast<ParkingRequest*>(
        MapViewOfFile(mapping.get(), FILE_MAP_ALL_ACCESS,
            0, 0, sizeof(ParkingRequest))));

    if (!slot) {
        throw std::runtime_error("Could not map shared memory.");
    }

    ParkingManager manager;
    int consumed = 0;

    while (true) {
        waitFor(full.get());
        ParkingRequest request = *slot;

        if (request.finished) {
            signal(empty.get());
            break;
        }

        std::shared_ptr<Vehicle> vehicle;
        const std::string type(request.type);

        if (type == "Motorcycle") {
            vehicle = std::make_shared<Vehicle::Motorcycle>(request.plate);
        } else if (type == "Car") {
            vehicle = std::make_shared<Vehicle::Car>(request.plate);
        } else if (type == "Bus") {
            vehicle = std::make_shared<Vehicle::Bus>(request.plate);
        } else {
            throw std::invalid_argument("Unknown vehicle type.");
        }

        manager.park(vehicle, request.destination);
        ++consumed;

        std::cout << "Consumed: " << request.plate << " parked at "
                  << request.destination << std::endl;

        signal(empty.get());
    }

    check(consumed == 3, "Incorrect request count.");
    check(manager.available() == 53, "Incorrect available space count.");
    check(manager.locations("BUS001") == std::vector<std::string>{
        "2-C-1", "2-C-2", "2-C-3", "2-C-4", "2-C-5"
    }, "Incorrect bus allocation.");

    std::cout << "Requests consumed: " << consumed << '\n';
    std::cout << "Available spaces: " << manager.available() << '\n';
    std::cout << "Shared-memory checks passed." << std::endl;
}

int main(int argc, char* argv[]) {
    try {
        if (argc != 2) {
            throw std::invalid_argument(
                "Use: ParkingSharedMemory.exe producer OR consumer");
        }

        const std::string mode(argv[1]);
        if (mode == "producer") {
            produce();
        } else if (mode == "consumer") {
            consume();
        } else {
            throw std::invalid_argument("Unknown mode.");
        }
        return 0;
    } catch (const std::exception& error) {
        std::cerr << "FAILED: " << error.what() << std::endl;
        return 1;
    }
}
