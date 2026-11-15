# Inter-process communication

## Message passing: Java sockets

ParkingSocketClient is the producer and ParkingSocketServer is the consumer.
They run in separate Java processes and connect through TCP port 5050
on 127.0.0.1.

The producer sends three parking requests followed by an END message.
The consumer receives each request, applies the parking rules and displays
the result. Application messages travel only from the client to the server.

The socket stream provides the communication channel and buffering.
This demonstration has one producer and one consumer.
It does not implement a separate application-level bounded queue.

Expected result: three requests consumed and 53 available spaces.

## Shared memory: C++ and Windows API

ParkingSharedMemory runs in producer or consumer mode in separate processes.
Both processes map the same named, page-file-backed memory area.

The shared buffer holds one parking request.
Two named semaphores represent an empty slot and a full slot.

The producer waits for an empty slot, writes a request and signals full.
The consumer waits for a full slot, reads and processes the request,
then signals empty. This prevents unread requests from being overwritten.

A final marker tells the consumer to stop.
Operating system handles and mapped views are released automatically.
Waits have a timeout so a missing process does not cause an endless wait.

Expected result: three requests consumed and 53 available spaces.

## Demonstration scope

Both mechanisms process motorcycle, car and bus parking requests using
the application's parking classes. Each consumer has its own in-memory
parking manager. These demos do not update the running web interface.

## References

https://learn.microsoft.com/windows/win32/memory/creating-named-shared-memory
https://learn.microsoft.com/windows/win32/sync/using-semaphore-objects
