#include <algorithm>
#include <cctype>
#include <iostream>
#include <memory>
#include <stdexcept>
#include <string>
#include <vector>

std::string normalize(std::string text) {
    const auto first = text.find_first_not_of(" \t\r\n");
    if (first == std::string::npos) {
        throw std::invalid_argument("Value is required.");
    }

    const auto last = text.find_last_not_of(" \t\r\n");
    text = text.substr(first, last - first + 1);

    for (char& character : text) {
        character = static_cast<char>(
            std::toupper(static_cast<unsigned char>(character)));
    }
    return text;
}

// Abstract base class with nested subclass declarations.
class Vehicle {
private:
    std::string plateNumber;

protected:
    explicit Vehicle(const std::string& plate)
        : plateNumber(normalize(plate)) {}

public:
    virtual ~Vehicle() = default;

    const std::string& plate() const {
        return plateNumber;
    }

    virtual std::string type() const = 0;
    virtual int spacesNeeded() const = 0;

    class Motorcycle;
    class Car;
    class Bus;
};

// Nested subclasses are defined after the base class is complete.
class Vehicle::Motorcycle final : public Vehicle {
public:
    explicit Motorcycle(const std::string& plate) : Vehicle(plate) {}
    std::string type() const override { return "Motorcycle"; }
    int spacesNeeded() const override { return 1; }
};

class Vehicle::Car final : public Vehicle {
public:
    explicit Car(const std::string& plate) : Vehicle(plate) {}
    std::string type() const override { return "Car"; }
    int spacesNeeded() const override { return 1; }
};

class Vehicle::Bus final : public Vehicle {
public:
    explicit Bus(const std::string& plate) : Vehicle(plate) {}
    std::string type() const override { return "Bus"; }
    int spacesNeeded() const override { return 5; }
};

enum class SpaceType { Motorcycle, Compact, Large };

struct ParkingSpace {
    int level;
    std::string row;
    int number;
    SpaceType type;
    std::shared_ptr<Vehicle> vehicle;

    std::string id() const {
        return std::to_string(level) + "-" + row + "-"
            + std::to_string(number);
    }

    bool accepts(const Vehicle& candidate) const {
        if (candidate.type() == "Motorcycle") {
            return true;
        }
        if (candidate.type() == "Car") {
            return type == SpaceType::Compact || type == SpaceType::Large;
        }
        return candidate.type() == "Bus" && type == SpaceType::Large;
    }
};

class ParkingManager {
private:
    std::vector<ParkingSpace> spaces;

    void addRow(int level, const std::string& row,
                int count, SpaceType type) {
        for (int number = 1; number <= count; ++number) {
            spaces.push_back({level, row, number, type, nullptr});
        }
    }

    std::size_t findSpace(const std::string& id) const {
        const std::string key = normalize(id);
        for (std::size_t i = 0; i < spaces.size(); ++i) {
            if (spaces[i].id() == key) {
                return i;
            }
        }
        throw std::invalid_argument("Parking space not found.");
    }

    std::shared_ptr<Vehicle> findVehicle(const std::string& plate) const {
        const std::string key = normalize(plate);
        for (const auto& space : spaces) {
            if (space.vehicle && space.vehicle->plate() == key) {
                return space.vehicle;
            }
        }
        return nullptr;
    }

    std::vector<std::size_t> allocation(
            const std::shared_ptr<Vehicle>& vehicle,
            const std::string& destination,
            bool moving) const {
        const auto& start = spaces.at(findSpace(destination));
        std::vector<std::size_t> result;

        // Validate every destination space before changing any record.
        for (int offset = 0; offset < vehicle->spacesNeeded(); ++offset) {
            const std::string id = std::to_string(start.level)
                + "-" + start.row + "-"
                + std::to_string(start.number + offset);

            const std::size_t index = findSpace(id);
            const auto& target = spaces[index];

            const bool occupiedByOther = target.vehicle
                && !(moving && target.vehicle == vehicle);

            if (occupiedByOther || !target.accepts(*vehicle)) {
                throw std::invalid_argument(
                    "Destination unavailable or unsuitable.");
            }

            result.push_back(index);
        }
        return result;
    }

public:
    ParkingManager() {
        addRow(1, "A", 5, SpaceType::Motorcycle);
        addRow(1, "B", 10, SpaceType::Large);
        addRow(1, "C", 5, SpaceType::Compact);

        addRow(2, "A", 5, SpaceType::Motorcycle);
        addRow(2, "B", 10, SpaceType::Compact);
        addRow(2, "C", 5, SpaceType::Large);

        addRow(3, "A", 5, SpaceType::Motorcycle);
        addRow(3, "B", 10, SpaceType::Compact);
        addRow(3, "C", 5, SpaceType::Compact);
    }

    int available() const {
        return static_cast<int>(std::count_if(
            spaces.begin(), spaces.end(),
            [](const ParkingSpace& space) {
                return !space.vehicle;
            }));
    }

    void park(const std::shared_ptr<Vehicle>& vehicle,
              const std::string& destination) {
        if (!vehicle) {
            throw std::invalid_argument("Vehicle is required.");
        }
        if (findVehicle(vehicle->plate())) {
            throw std::invalid_argument("Vehicle is already parked.");
        }

        const auto targets = allocation(vehicle, destination, false);
        for (std::size_t index : targets) {
            spaces[index].vehicle = vehicle;
        }
    }

    void move(const std::string& plate, const std::string& destination) {
        const auto vehicle = findVehicle(plate);
        if (!vehicle) {
            throw std::invalid_argument("Vehicle not found.");
        }

        const auto targets = allocation(vehicle, destination, true);

        for (auto& space : spaces) {
            if (space.vehicle == vehicle) {
                space.vehicle.reset();
            }
        }
        for (std::size_t index : targets) {
            spaces[index].vehicle = vehicle;
        }
    }

    int depart(const std::string& plate) {
        const auto vehicle = findVehicle(plate);
        if (!vehicle) {
            throw std::invalid_argument("Vehicle not found.");
        }

        int released = 0;
        for (auto& space : spaces) {
            if (space.vehicle == vehicle) {
                space.vehicle.reset();
                ++released;
            }
        }
        return released;
    }

    std::vector<std::string> locations(const std::string& plate) const {
        const std::string key = normalize(plate);
        std::vector<std::string> result;

        for (const auto& space : spaces) {
            if (space.vehicle && space.vehicle->plate() == key) {
                result.push_back(space.id());
            }
        }
        return result;
    }
};

void check(bool condition, const std::string& message) {
    if (!condition) {
        throw std::runtime_error(message);
    }
}

int main() {
    try {
        ParkingManager manager;
        check(manager.available() == 60, "Incorrect initial capacity.");

        manager.park(std::make_shared<Vehicle::Motorcycle>("MOTO01"), "1-A-1");
        manager.park(std::make_shared<Vehicle::Car>("CAR123"), "1-C-1");
        manager.park(std::make_shared<Vehicle::Bus>("BUS001"), "1-B-1");

        check(manager.available() == 53, "Incorrect occupied space count.");
        std::cout << "Available after parking: " << manager.available() << '\n';

        // Moving a bus may overlap some of its own current spaces.
        manager.move("bus001", "1-B-3");
        check(manager.locations("BUS001") == std::vector<std::string>{
            "1-B-3", "1-B-4", "1-B-5", "1-B-6", "1-B-7"
        }, "Incorrect bus movement.");

        std::cout << "Bus moved to spaces 1-B-3 through 1-B-7.\n";

        try {
            manager.move("BUS001", "3-B-1");
            throw std::runtime_error("An invalid bus move was accepted.");
        } catch (const std::invalid_argument& error) {
            std::cout << "Blocked: " << error.what() << '\n';
        }

        check(manager.available() == 53, "Blocked move changed the count.");
        check(manager.locations("BUS001") == std::vector<std::string>{
            "1-B-3", "1-B-4", "1-B-5", "1-B-6", "1-B-7"
        }, "Blocked move changed the location.");

        manager.move("BUS001", "2-C-1");
        check(manager.locations("BUS001") == std::vector<std::string>{
            "2-C-1", "2-C-2", "2-C-3", "2-C-4", "2-C-5"
        }, "Incorrect level 2 allocation.");

        check(manager.depart("BUS001") == 5, "Bus departure failed.");
        check(manager.available() == 58, "Incorrect departure count.");

        manager.depart("CAR123");
        manager.depart("MOTO01");
        check(manager.available() == 60, "Parking did not become empty.");

        std::cout << "Available after all departures: "
                  << manager.available() << '\n';
        std::cout << "C++ parking checks passed.\n";
        return 0;
    } catch (const std::exception& error) {
        std::cerr << "FAILED: " << error.what() << '\n';
        return 1;
    }
}
