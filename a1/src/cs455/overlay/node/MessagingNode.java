package cs455.overlay.node;


import cs455.overlay.util.Interpreter;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.*;

public class MessagingNode implements Node {

	private String registryIp;
	private int registryPort;
	private Socket registryConnection;
	private TCPSender registrySender;
	
	// Keep track of connected messaging nodes' listening addresses and socket connections to them
	// These sockets may be because of initiating or accepting connections
	private HashMap<String, Socket> connections = new HashMap<String, Socket>();
	
	// Messaging nodes listening addresses matched with sockets connected to neighboring nodes
	// that would result in the shortest path
	private HashMap<String, Socket> routingEntries = new HashMap<String, Socket>();
	private ArrayList<String> overlayNodes = new ArrayList<String>();
	
	// Rebuilt every time populateRoutingEntries() is called
	private HashMap<String, String> shortestPaths;
			
	private MessagingNodeListener messagingNodeListener;
	private MessagingNodeReceiver registryConnectionReceiver;
	
	private AtomicInteger receiveTracker = new AtomicInteger(0);
	private AtomicInteger sendTracker = new AtomicInteger(0);
	private AtomicInteger relayTracker = new AtomicInteger(0);
	
	private AtomicLong sendSummation = new AtomicLong(0);
	private AtomicLong receiveSummation = new AtomicLong(0);

	public boolean connectToRegistry()
	{
		boolean success = false;
		try
		{
			registryConnection = new Socket(registryIp, registryPort);
			success = true;
		}
		catch(IOException e)
		{
			System.out.println("Can't connect to Registry. Exiting...");
			System.exit(0);
		}
		
		registrySender = new TCPSender(registryConnection);
		registryConnectionReceiver = new MessagingNodeReceiver(registryConnection);
		new Thread(registryConnectionReceiver).start();
		return success;
	}
	
	public void sendEventRegisterRequest() throws IOException
	{
		RegisterRequest ev = new RegisterRequest(registryConnection.getLocalAddress().getHostAddress(), messagingNodeListener.getLocalPort());
		registrySender.send(ev.getBytes());
		//System.out.println("Sending register request");
	}
	
	private void sendEventDeregisterRequest() throws IOException
	{
		DeregisterRequest ev = new DeregisterRequest(registryConnection.getLocalAddress().getHostAddress(), messagingNodeListener.getLocalPort());
		registrySender.send(ev.getBytes());
		//System.out.println("Sending deregister request");
	}
	
	private synchronized void exit()
	{
		try
		{
			for(Socket s : connections.values())
				s.close();
			
			registryConnection.close();
			messagingNodeListener.close();
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
		}
		finally 
		{
			System.exit(0);
		}
	}
	
	@Override
	public void onEvent(Event ev) {
		
		switch(ev.getType())
		{
		case REGISTER_RESPONSE:
			System.out.println(((RegisterResponse) ev).getInfo());
			break;
		case DEREGISTER_RESPONSE:
			System.out.println(((DeregisterResponse) ev).getInfo());
			if (((DeregisterResponse) ev).getStatus())
				exit();
			break;
		case MESSAGING_NODES_LIST:
			onEvent((MessagingNodesList) ev);
			break;
		case LINK_WEIGHTS_LIST:
			onEvent((LinkWeightsList) ev);
			break;
		case TASK_INITIATE:
			onEvent((TaskInitiate) ev);
			break;
		case MESSAGE:
			onEvent((Message) ev);
			break;
		case PULL_TRAFFIC_SUMMARY:
			onEvent((PullTrafficSummary) ev);
			break;
		default:
			break;
		}
	}
	
	private void onEvent(PullTrafficSummary ev)
	{
		String ownIpAddress = registryConnection.getLocalAddress().getHostAddress();
		int portnum = messagingNodeListener.getLocalPort();
		TrafficSummary ts = new TrafficSummary(ownIpAddress, portnum, sendTracker.intValue(), sendSummation.longValue(), receiveTracker.intValue(), receiveSummation.longValue(), relayTracker.intValue());
		
		try
		{
			registrySender.send(ts.getBytes());
			
			// Reset counters
			sendTracker.set(0);
			sendSummation.set(0);
			receiveTracker.set(0);
			receiveSummation.set(0);
			relayTracker.set(0);
		}
		catch(IOException e)
		{
			System.out.println("Can't send traffic summary to registry");
		}
		
	}
	
	private void onEvent(PeerConnect ev, Socket s)
	{
		String ipAddress = ev.getIpAddress();
		int port = ev.getPort();
		
		if (ipAddress.equals(s.getInetAddress().getHostAddress()))
		{
			synchronized(connections)
			{
				System.out.println("Accepted connection request from " + ipAddress + ":" + port);
				connections.put(ipAddress + ":" + port, s);
			}
		}
		else
		{
			System.out.println("Bad peer connection");
			try
			{
				s.close();
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
			}
		}
		
	}
	
	private void onEvent(Message ev)
	{
		String destination = ev.getDestination();
		String ownAddress = registryConnection.getLocalAddress().getHostAddress() + ":" + messagingNodeListener.getLocalPort();
		if (destination.equals(ownAddress))
		{
			receiveSummation.addAndGet(ev.getPayload());
			receiveTracker.getAndIncrement();
			return;
		}
		else
		{
			try
			{
				TCPSender t = new TCPSender(routingEntries.get(destination));
				t.send(ev.getBytes());
				relayTracker.getAndIncrement();
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
				System.exit(0);
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				System.out.println("Destination: " + destination);
				System.out.println(routingEntries.toString());
				System.out.println("Own address: " + ownAddress);
				throw e;
			}
		}
	}
	
	private void onEvent(TaskInitiate ev)
	{
		int numRounds = ev.getRounds();
		
		int numMsgPerRound = 5;
		
		for(int i=0; i<numRounds; i++)
		{
			// Choose a random sink node from the entire overlay except itself
			// RoutingEntries is guaranteed to have every other node except itself
			int randomKey = ThreadLocalRandom.current().nextInt(0, routingEntries.size());
			String destination = overlayNodes.get(randomKey);
			Socket s = routingEntries.get(destination);
			
			String[] destinationParsed = destination.split(":");
			for (int j=0; j<numMsgPerRound; j++)
			{
				int payload = ThreadLocalRandom.current().nextInt();
				// Send to sink node
				try
				{
					TCPSender t = new TCPSender(s);
					t.send(new Message(new InetSocketAddress(destinationParsed[0], Integer.parseInt(destinationParsed[1])), payload).getBytes());
					sendTracker.getAndIncrement();
					sendSummation.addAndGet(payload);
				}
				catch(IOException e)
				{
					System.out.println(e.getMessage());
					System.exit(0);
				}
			}
		}
		
		try
		{
			registrySender.send(new TaskComplete(registryConnection.getLocalAddress().getHostAddress(), messagingNodeListener.getLocalPort()).getBytes());
		}
		catch(IOException e)
		{
			System.out.println("Error in sending task completing status");
		}
	}
	
	private void populateRoutingEntries(LinkWeightsList ev)
	{ 
		HashMap<String, Integer> mapHostNameToInt = new HashMap<String, Integer>();
		Vector<String> mapIntToHostName = new Vector<String>();
		
		int nodeCount = 0;
		// Extract listening addresses of all messaging nodes
		for(LinkWeightsList.LinkInfo l : ev)
		{
			String k1 = l.getAddressA().getHostString() + ":" + l.getAddressA().getPort();

			//System.out.println(k1);
			if(!mapHostNameToInt.containsKey(k1))
			{
				//System.out.println("done");
				mapHostNameToInt.put(k1, nodeCount);
				mapIntToHostName.add(k1);
				nodeCount++;
			}
			
			String k2 = l.getAddressB().getHostString() + ":" + l.getAddressB().getPort();

			//System.out.println(k2);
			if(!mapHostNameToInt.containsKey(k2))
			{
				//System.out.println("done");
				mapHostNameToInt.put(k2, nodeCount);
				mapIntToHostName.add(k2);
				nodeCount++;
			}
		}
		
		//System.out.println(mapHostNameToInt.size());
		
		final int infinity = Integer.MAX_VALUE;
		int[][] graph = new int[nodeCount][nodeCount];
		for (int[] row: graph)
		{
			Arrays.fill(row, infinity);
		}
		
		for(LinkWeightsList.LinkInfo l : ev)
		{
			String k1 = l.getAddressA().getHostString() + ":" + l.getAddressA().getPort();
			String k2 = l.getAddressB().getHostString() + ":" + l.getAddressB().getPort();

			int i = mapHostNameToInt.get(k1);
			int j = mapHostNameToInt.get(k2);
			
			graph[i][j] = graph[j][i] = l.getWeight();
			
		}
		
		// So now our graph consists of only infinity values and weights in case there is a link
		
		// Distances to infinity
		Integer distance[] = new Integer[nodeCount];
		
		// Previous to Undefined
		int[] prev = new int[nodeCount];
		HashSet<Integer> vertices = new HashSet<Integer>();
		
		final int undefined = -1;
		for(int i=0; i<nodeCount; i++)
		{
			prev[i] = undefined;
			distance[i] = infinity;
			vertices.add(i);
		}
		
		int source = mapHostNameToInt.get(registryConnection.getLocalAddress().getHostAddress() + ":" + messagingNodeListener.getLocalPort());
		
		distance[source] = 0;
		
		while(vertices.size() > 0)
		{
			// Pick up the node with minimum distance
			// will be source node at start
			
			int minDistance=infinity;
			int u=0;
			for(int t=0; t<nodeCount; t++)
			{
				if(vertices.contains(t) && distance[t] < minDistance)
				{
					minDistance = distance[t];
					u = t;
				}
			}
			
			vertices.remove(u);
			
			// For all nodes in the graph
			for(int v=0; v<nodeCount; v++)
			{
				// If it is a neighbor
				if (graph[u][v] != infinity)
				{
					
					int altDistance = distance[u] + graph[u][v];
					if (altDistance < distance[v])
					{
						distance[v] = altDistance;
						prev[v] = u;
					}
				}
			}
		}
		
		//System.out.println(mapHostNameToInt.toString());
		//System.out.println(mapIntToHostName.toString());
		
		//System.out.println(Arrays.toString(distance));
		//System.out.println(Arrays.toString(prev));
		
		
		for (int i = 0; i<nodeCount; i++)
		{
			int j = i;
			if (prev[j] != undefined)
			{
				while(prev[j] != source)
					j = prev[j];
				synchronized(connections)
				{
					// a packet sent to i should be sent to j by this messaging node	
					routingEntries.put(mapIntToHostName.get(i), connections.get(mapIntToHostName.get(j)));
					overlayNodes.add(mapIntToHostName.get(i));
				}
			}	
		}
		
		// Rebuild it every time
		shortestPaths = new HashMap<String, String>();
		for(int i=0; i<nodeCount; i++)
		{
			// Build shortest path if destination is i from source
			int j = i;
			String path = mapIntToHostName.get(j);
			while(prev[j] != undefined)
			{
				path += " --- " + graph[j][prev[j]] + " --- " + mapIntToHostName.get(prev[j]);
				j = prev[j];
			}
			// Destination is the key
			shortestPaths.put(mapIntToHostName.get(i), path);
		}
		
		/**
		for(Map.Entry<String,Socket> s : routingEntries.entrySet())
		{
			if (s.getValue() == null)
				System.out.println(s.getKey() + "----");
			else
				System.out.println(s.getKey() + "----" + s.getValue().getInetAddress().getHostAddress() + ":" + s.getValue().getPort());
		}
		System.out.println("----");
		synchronized(connections)
		{
			for(Map.Entry<String,Socket> s : connections.entrySet())
			{
				if (s.getValue() == null)
					System.out.println(s.getKey() + "----");
				else
					System.out.println(s.getKey() + "----" + s.getValue().getInetAddress().getHostAddress() + ":" + s.getValue().getPort());
			}
		}
		*/
	}
	
	private void printShortestPath()
	{
		for (Map.Entry<String, String> entry : shortestPaths.entrySet())
		{
			System.out.println(entry.getKey() + "  :  "  + entry.getValue());
		}
	}
	
	private void onEvent(LinkWeightsList ev)
	{
		populateRoutingEntries(ev);	
		System.out.println("Link weights are received and processed. Ready to send messages");
	}
	
	private void onEvent(MessagingNodesList ev)
	{
		for(InetSocketAddress a: ev)
		{
			try
			{
				Socket s = new Socket(a.getHostString(), a.getPort());
				TCPSender t = new TCPSender(s);
				t.send(new PeerConnect(registryConnection.getLocalAddress().getHostAddress(), messagingNodeListener.getLocalPort()).getBytes());
				synchronized(connections)
				{
					// Store the socket for sending data
					connections.put(a.getHostString() + ":" + a.getPort(), s);
				}
				
				// Start a thread for listening on this socket
				new Thread(new MessagingNodeReceiver(s)).start();
				
				System.out.println("Connected successfully to " + a.getHostString() + ":" + a.getPort());
			}
			catch(IOException e)
			{
				System.out.println("Can't connect to " + a.getHostString() + ":" + a.getPort());
				return;
			}
		}
		System.out.println("All connections are established. Number of connections: " + ev.size());
	}
	
	public MessagingNode(String registryIp, int registryPort)
	{
		this.registryIp = registryIp;
		this.registryPort = registryPort;
		
		this.messagingNodeListener = new MessagingNodeListener();
		
		new Thread(this.messagingNodeListener).start();
		
	}
	
	public static void main(String[] args)
	{
		if (args.length == 2)
		{
			String registryIp = args[0];
			int registryPort = Integer.parseInt(args[1]);
			
			// Initialize this messaging node
			MessagingNode m = new MessagingNode(registryIp, registryPort);
			
			if (m.connectToRegistry())
			{
				System.out.println("Connected to registry");
				try
				{
					m.sendEventRegisterRequest();
					
				}
				catch(IOException e)
				{
					System.out.println("Couldn't send register request to Registry: " + e.getMessage());
					System.exit(0);
				}
			}
			else
			{
				System.out.println("Unable to connect to registry");
				System.exit(0);
				
			}
			
			m.new MessagingNodeInterpreter().run();
			
			//Dead code follows
			
		}
		else
		{
			System.out.println("Usage: cs455.overlay.node.MessagingNode <registry-ip> <registry-port>");
			System.exit(0);
		}
	}
	
	private class MessagingNodeListener extends TCPListenerThread
	{
		public MessagingNodeListener()
		{
			super();
			//System.out.println("Listening at " + super.sock.getInetAddress().getHostAddress() + ":" + super.sock.getLocalPort());
		}
		
		public void handleClient(Socket s)
		{
			// Add the received connection to send messages later
			Thread t = new Thread(new MessagingNodeReceiver(s));
			t.setName("Messaging node: " + super.sock.toString());
			t.start();
		}
	}
	
	private class MessagingNodeReceiver extends TCPReceiverThread
	{
		public MessagingNodeReceiver(Socket s)
		{
			super(s);
		}
		
		private Event readEventRegisterResponse() throws IOException
		{
			boolean status = din.readByte() == 1 ? true : false;
			String additionalInfo = din.readUTF();
			
			return new RegisterResponse(status, additionalInfo);
		}
		
		private Event readEventDeregisterResponse() throws IOException
		{
			boolean status = din.readByte() == 1 ? true : false;
			String additionalInfo = din.readUTF();
			
			return new DeregisterResponse(status, additionalInfo);
		}
		
		private Event readEventMessagingNodesList() throws IOException
		{
			int count = din.readInt();
			MessagingNodesList ev = new MessagingNodesList();
			
			for(int i = 0; i < count; i++)
			{
				String ip = din.readUTF();
				int port = din.readInt();
				ev.add(ip, port);
			}
			
			return ev;
		}
		
		private LinkWeightsList.LinkInfo readLinkInfo(LinkWeightsList l) throws IOException
		{
			String ipAddressA = din.readUTF();
			int portnumA = din.readInt();
			String ipAddressB = din.readUTF();
			int portnumB = din.readInt();
			int weight = din.readInt();
			
			LinkWeightsList.LinkInfo linfo = l.new LinkInfo(new InetSocketAddress(ipAddressA, portnumA), new InetSocketAddress(ipAddressB, portnumB), weight);
			
			return linfo;
		}
		
		private Event readEventLinkWeightsList() throws IOException
		{
			LinkWeightsList ev = new LinkWeightsList();
			
			int count = din.readInt();
			for (int i=0; i<count; i++)
			{
				ev.add(readLinkInfo(ev));
			}
			
			return ev;
		}
		
		private Event readEventTaskInitiate() throws IOException
		{
			int numRounds = din.readInt();
			return new TaskInitiate(numRounds);
		}
		
		private Event readEventMessage() throws IOException
		{
			String ipAddress = din.readUTF();
			int port = din.readInt();
			InetSocketAddress a = new InetSocketAddress(ipAddress, port);
			int payload = din.readInt();
			return new Message(a, payload);
		}
		
		private Event readEventPeerConnect() throws IOException
		{
			String ipAddress = din.readUTF();
			int port = din.readInt();
			
			return new PeerConnect(ipAddress, port);
			
		}
		
		private Event readEventPullTrafficSummary() throws IOException
		{
			return new PullTrafficSummary();
		}
		
		public void handleEvent(EventType evType)
		{
			Event ev = null;
			try
			{
				switch(evType)
				{
				case REGISTER_RESPONSE:
					ev = readEventRegisterResponse();
					onEvent(ev);
					break;
				case MESSAGING_NODES_LIST:
					ev = readEventMessagingNodesList();
					onEvent(ev);
					break;
				case LINK_WEIGHTS_LIST:
					ev = readEventLinkWeightsList();
					onEvent(ev);
					break;
				case TASK_INITIATE:
					ev = readEventTaskInitiate();
					onEvent(ev);
					break;
				case PEER_CONNECT:
					ev = readEventPeerConnect();
					onEvent((PeerConnect)ev, super.sock);
					break;
				case MESSAGE:
					ev = readEventMessage();
					onEvent(ev);
					break;
				case PULL_TRAFFIC_SUMMARY:
					ev = readEventPullTrafficSummary();
					onEvent(ev);
					break;
				case DEREGISTER_RESPONSE:
					ev = readEventDeregisterResponse();
					onEvent(ev);
					break;
				default:
					System.out.println("Unknown event encountered");
					break;
				}
			}
			catch(IOException e)
			{
				System.out.println("Can't read event: " + e.getMessage());
				System.exit(0);
			}
		}
	}

	private class MessagingNodeInterpreter extends Interpreter
	{
		public MessagingNodeInterpreter()
		{
			super(">> ", 0);
		}
		
		private boolean handleSingleWordCommands(String[] words)
		{
			boolean isValid = true;
			
			if (words.length != 1)
			{		
				isValid = false;
			}
			
			if (!isValid)
				System.out.println("Usage: " + words[0]);
			
			return isValid;
		}
		
		private boolean handlePrintShortestPath(String[] words)
		{
			boolean isValid = handleSingleWordCommands(words);
			if (isValid)
			{
				printShortestPath();
			}
			return isValid;
		}
		
		private boolean handleExitOverlay(String[] words)
		{
			boolean isValid = handleSingleWordCommands(words);
			
			if (isValid)
			{
				try
				{
					sendEventDeregisterRequest();
				}
				catch(IOException e)
				{
					System.out.println(e.getMessage());
				}
			}
			
			return isValid;
		}
		
		public boolean handleCommand(String cmd)
		{
			String[] words = cmd.trim().split("\\s+");
			
			boolean isValid = true;
			
			if (words.length > 0 && words[0].length() > 0)
			{
				switch(words[0].trim())
				{
				case "print-shortest-path":
					isValid = handlePrintShortestPath(words);
					break;
				case "exit-overlay":
					isValid = handleExitOverlay(words);
					break;
				default: 
					post("Unknown command: " + words[0]);
					isValid = false;
					break;
				}
			}
			
			return isValid;
		}
	}
}
