# Using Dijkstra’s Shortest Paths to Route Packets in a Network Overlay

## Aim
1. Constructing a logical overlay over a distributed set of nodes, and then
2. Computing shortest paths using Dijkstra’s algorithm to route packets in the system.

## Requirements

### Overlay
* The overlay will contain at least M messaging nodes (default of 10).
* Each messaging node will be connected to N (default of 4) other messaging nodes.
* Should not have any partition
* Not all overlays meet above two criteria. May/may not want to place a check against it.
* No messaging node should have a link to itself.


### Links
* Links are bidirectional i.e. if messaging node A established a connection to messaging node B, then messaging node B must use that link to communicate with A.
* Each link that connects two messaging nodes within the overlay has a weight associated with it.

### Communication
* TCP only

### Registry
* Single instance within the system
* Should run as a foreground process, accepting commands while running
* Accept command line arguments as `java cs455.overlay.node.Registry <portnum>`
* Allow messaging nodes to register with it (thus it keeps information about all the nodes in the overlay)
  * Should check if the IP address and port number in the request match with that of the requesting node
  * Should also check against double registration
  * Response format `REGISTER_RESPONSE | SUCCESS or FAILURE | Additional info` as `int | byte | string`
* Allow messaging nodes to deregister with it
* Brokers the connections between messaging nodes, thereby orchestrating the construction of the overlay.
  * Select N messaging nodes different from the intended messaging node randomly for each of the M messaging nodes
  * Keep track of the connections thus created in the form of connection counts for both linked nodes.
  * Ensure that the connection counts are exactly met
  * The connections are orchestrated by sending a list of peers to each of the messaging nodes
    * Since the links are bidirectional, it must ensure that nodes are not repeated in the peer list, lest same link be established twice. This means that the length of peer list may range from 0 to N.
    * Format `MESSAGING_NODES_LIST | Length of peer list | Info for peer 1 | ... | info for last peer`
  
* Assign and publish weights to the links between messaging nodes on a scale from 1 to 10.
  * Weights are chosen randomly
  * Format `LINK_WEIGHTS | Number of links | [hostname:port] [hostname:port] [link weight] for link 1 | ... | ... for link L`
* Initiate communications between the messaging nodes
  * Format `TASK_INITIATE | Rounds`
* Interactions with messaging nodes are in the form of request-respose messages
* Does not play any role in routing data within the overlay
* Pull network traffic summary
  * Wait until all `TASK_COMPLETE` messages are received
  * Wait some time (15s) to allow messages still in transition
  * Issue message `PULL_TRAFFIC_SUMMARY` to all the registerd nodes in the system

* Commands:
  * `list-messaging-nodes` List information about the messaging nodes (hostname, and port-number), each on a separate line
  * `list-weights` List information about links (ipaddress:port ipaddress:port weight) comprising the overlay, each on a separate line.
  * `setup-overlay <number-of-connections>` Setup the overlay. The `number-of-connection` is the value for `N`. Handle the error when `M` < `N`.
    * Once the overlay is setup, no need to deal with a node registering or deregistering.
  * `send-overlay-link-weights` Send `LINK_WEIGHTS` message to all the messaging nodes. This makes the messaging nodes aware of all the nodes in the overlay as well as link weights between them.
  * `start <number-of-rounds>` The registry sends the `TASK_INITITATE` messages to all the registered messaging nodes. Each node will be sending a number of messages in `number-of-rounds` rounds.
  
#### Comments
* Registry must keep socket connections to all the messaging nodes to enable request-response messages

### Messaging nodes
* Should run as a foreground process, accepting commands while running
* Accept command line arguments as `java cs455.overlay.node.MessagingNode <registry-host> <registry-port>`
* Register with the registry when starting up for the first time.
  * Register IP address and port
  * Should be possible to register messaging nodes running on the same host, but communicating over different ports
  * Request format `REGISTER_REQUEST | Ip address | port` as `int | string | int`.
  * Additional message in case of success should indicate the number of messaging nodes currently known to registry.
  * Additional message in case of failure should indicate the reason for failure.
  * If the registry is unable to communicate with a messaging node that requested to be registered, assume it to be dead, and remove from overlay
  * Initialize `sendTracker`, `relayTracker` and `receiveTracker` to 0.
    * The `sendTracker` represents the number of messages that were sent by that node
    * The `receiveTracker` maintains information about the number of messages that were received
    * The  `relayTracker` tracks the number of messages relayed by the node – i.e., messages for which it was neither the source nor the sink.
    * The summation of `sendTracker` will be exactly M * number of messages per round * number of rounds
    * Each `receiveTracker` will be a multiple of number of messages per round, but sums to exactly equal to summation of `sendTracker`
  * Initialize `sendSummation` and `receiveSummation` to 0
    * Both are of type `long` to prevent overflow
    * Denote the summation of message payload that are sent and received by a node
    * The summation of `sendSummation` and `receiveSummation` over all the nodes should be equal to each other
    * The values of `sendSummation` and `receiveSummation` at a node can be positive or negative.
    
 
* Deregister with the registry when exiting the overlay
  * Request format `DEREGISTER_REQUEST | IP address | port`
  * Should check against a mismatch between addresses or a missing registry entry for the node
* Can be more than or equal to 10 in number
* Each messaging node needs to automatically configure the ports over which it listens for communications i.e. the port numbers should not be hard-coded or specified at the command line.
* Uses `TCPServerSocket` to accept incoming connections
* Establish connections with other messaging nodes when registry sends `MESSAGING_NODES_LIST` message
  * Print a message on success: `All connections are established. Number of connections: x` 

* Process the `LINK_WEIGHTS` message from the registry
  * Store information from it to generate routing paths
  * Acknowledge the receipt and processing of this message by printing `Link weights are received and processed. Ready to send messages.`

* Should keep a count of the number of messages relayed through it

* Sending message in the form of a randomly generated number
  * Send a certain number of messages as specified by the number of rounds
  * Source node should compute the shortest path for the message to the sink node
  * Path information is included in the message
  * Routing plans may be cached by the relay nodes as an optimization
  * No message should be received by the same node more than once.

* Acknowlegde the completion to registry in the form of: `TASK_COMPLETE | IP address | port`
* Send traffic summary
  * Upon receipt of `PULL_NETWORK_SUMMARY` message from the registry, send a response of the format: `TRAFFIC_SUMMARY | Ip address | port | Number of sent messages | Summation of sent messages | Number of received messages | Summation of received messages | Number of messages relayed`
  * Reset the counters for sent, received, their summations, and relayed messages
  
* Commands:
  * `print-shortest-path`Print shortest path to all other messaging nodes in the system in the form of `node--weight--node--...--node`.
  * `exit-overlay` Send a deregisteration request to the registry, await its response before terminating its process.
  
#### Comments
* Every message node must keep a socket connection to the registry, as well as its N directly connected message nodes.
* Routing plans should be cached as node -> node entries

### Messages
* Each message will have a specific wire format, which can be customized, but should include the needed fields
* Preferably diffeent classes for each message format.
* Use of Java serialization is not allowed. Use marshalling and demarshalling instead.

## Procedure

1. Setup the overlay. This includes assigning weights to the links so established.
  * Send peer list messages to each of the messaging nodes using `MESSAGING_NODES_LIST` message

2. Send link weights to each of the messaging nodes using `LINK_WEIGHTS` message. This is only done once after setting up the overlay via `send-overlay-link-weights` command. Nodes will not be added to the overlay from hereon.

3. Upon receving the `start` command, the registry sends the `TASK_INITITATE` messages to all the registered messaging nodes. Then:
  * Each node will start exchanging messages with other nodes.
  * The communication is not direct, rahter through the overlay.
  * A fixed number of messages are sent by each node in *rounds*
  * Each round involves a node (**source node**) sending messages to a randomly chosen node other than itself, called the **sink node**. The sink node is calculated every round. The source node knows other nodes' addresses from the `LINK_WEIGHTS` message received earlier from the registry
  * Before the message can be sent, the node calculates the shortest path to the destination and encodes it into the message. The shortest route may pass through intermediate nodes, which relay these packets.
  * The payload is computed as a randome 4 byte signed integer
  * A fixed number of messages are sent to the sink node.
  * The whole process is repeated by each messaging node *rounds* number of times
  * Open only one connection for sending data, keep multiple connections open for receiving from other nodes

4. As each node completes its transmissions, it will send a `TASK_COMPLETE` message to the registry

5. Upon receiving `TASK_COMPLETE` messages from all the nodes in the overlay, the registry waits a few seconds before issuing the `PULL_TRAFFIC_SUMMARY` message, to which each node replies via `TRAFFIC_SUMMARY` message.

6. Upon receipt of the `TRAFFIC_SUMMARY` from all the registered nodes, the registry will proceed to print out the summary table. Each row must be printed on a separate line.

## Verification

1. Ensure that the number of messages that you send and receive within the system match, and
2. These messages have not been corrupted in transit to the intended recipient. This is verified by comparing the summation of `sendSummation` and `receiveSummation` over all the nodes in the overlay, which in turn should be equal to each other.
