Description of the files:

All files reside under cs455.scaling. There are sub-packages as described below:

cs455.scaling.works		Consists of task abstractions like ReadWork, HashWork, etc.

cs455.scaling.util 		Consists of utility classes:

						BlockingQueue is a thread-safe implementation of a queue that blocks if dequeue is called on an empty queue

						Hasher is a thread-safe convenience class for SHA1 hashing byte buffers

cs455.scaling.server	Consists of classes related to Server side:

						Server is an abstraction for server node

						ThreadPoolManager is an implementation for thread pool manager which manages Works and Workers

						Worker is an abstraction for worker threads

cs455.scaling.client	Consists of client side classes:

						Client is an abstraction for client node

						Payload is an abstraction for the 8KB random data that the client sends.
