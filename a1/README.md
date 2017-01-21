#Using Dijkstra’s Shortest Paths to Route Packets in a Network Overlay

## Aim
1. Constructing a logical overlay over a distributed set of nodes, and then
2. Computing shortest paths using Dijkstra’s algorithm to route packets in the system.

## Requirements

### Overlay
* The overlay will contain at least 10 messaging nodes, and each messaging node will be connected to N
(default of 4) other messaging nodes.

### Links
* Links are bidirectional i.e. if messaging node A established a connection to messaging node B, then messaging node B must use that link to communicate with A.
* Each link that connects two messaging nodes within the overlay has a weight associated with it.

### Communication
* TCP only

## Procedure

1. Setup the overlay. This includes assigning weights to the links so established.

2. Messaging nodes in the system will select a **sink node** at random and send that node a message. The communication is not direct, rahter through the overlay. This is done by computing the shortest route (based on the weights assigned during overlay construction) between the **source node** and the sink node. The shortest route may pass through intermediate nodes, which relay these packets.


## Verification

1. Ensure that the number of messages that you send and receive within the system match, and
2. These messages have not been corrupted in transit to the intended recipient.
