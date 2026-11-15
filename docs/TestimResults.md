# Testim UI Test Results

Date: 15 November 2026
Test: Parking - Car registration and departure
Environment: Chrome on Windows, local Java server
URL: http://127.0.0.1:8080/

## Test steps
1. Enter plate TEST001.
2. Select Car.
3. Select an available compact space.
4. Park the vehicle.
5. Register the vehicle departure.
6. Validate the message: Vehicle departed. Spaces released: 1

## Actual result
PASS. All recorded steps and the final text validation passed.
The complete automated test took 23 seconds. This includes browser
setup and test steps; it is not the response time of one parking action.

## Issues and retest
Earlier runs failed during the plate input step with a timeout.
Some runs also reported a viewport size warning.
The viewport configuration was adjusted and Native events was enabled
for the plate input step. The latest run passed without a viewport warning.
The exact cause of the earlier failures was not confirmed.

## Evidence
A screenshot of the successful Testim run is kept with the assessment evidence.
