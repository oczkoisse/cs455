Relevant notes:

Most of the classes are according to format suggested during the lab sessions. However, I have made a few changes:

- TCPListenerThread is an interface to be implemented each by RegistryListener and MessagingNodeListener classes, both of which are inner classes of Registry and MessagingNode respectively. The advantages include easier way to pass Socket objects rather than parameter passing.

- Similary, TCPReceiverThread is also an interface to be implemented by RegistryReceiver and MessagingNodeReceiver classes, again both of which are inner classes of Registry and MessagingNode respectively.

- The assignment asks us to register MessagingNode with Registry. The way I have implemented is by sending the listening addresses of MessagingNode in the registration request. This is assumed to be the identity of nodes thereon.

- After the overlay was setup, and connections established among messaging nodes, the messaging nodes had no way to identify the listening addresses of messaging nodes they accepted connections from. This would preclude me to implement the routing approch taken below, so I also implemented a PEER_CONNECT message that was sent by each messaging node just after connecting to other messaging node, which contained its listening address.

- Instead of encoding the entire routing information in the message, I adopted a simpler but equivalent approach in my opinion. This was enabled by identifying each messaging node by its listening address, and applying Djikstra's algorithm on the information received from the Registry in the form of Link weights. Then, a routing table was constructed, which identifed a socket connection to forward the message to whenever the destination field of MESSAGE contained an IP address other than its own. The socket to which the message will be forwarded is actually the shortest route the message can take on its way to the destination.
