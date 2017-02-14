package cs455.overlay.node;

import cs455.overlay.util.Interpreter;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MessagingNode implements Node {

	private String registryIp;
	private int registryPort;
	private Socket registryConnection;
	private TCPSender registrySender;
	
	// Keep track of addresses of messaging nodes' listening addresses and socket connections to them
	// These sockets may be because of initiating or accepting connections
	private HashMap<String, Socket> connections = new HashMap<String, Socket>();
	
	// Messaging nodes listening addresses matched with sockets connected to neighboring nodes
	// that would result in the shortest path
	private HashMap<String, Socket> routingEntries = new HashMap<String, Socket>();
	private ArrayList<String> overlayNodes = new ArrayList<String>();
	
	private MessagingNodeListener messagingNodeListener;
	private MessagingNodeReceiver registryConnectionReceiver;
	
	private int receiveTracker = 0;
	private int sendTracker = 0;
	private int relayTracker = 0;
	
	private long sendSummation = 0;
	private long receiveSummation = 0;
	
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
		System.out.println("Sending register request");
	}
	
	private void sendEventDeregisterRequest() throws IOException
	{
		DeregisterRequest ev = new DeregisterRequest(registryConnection.getLocalAddress().getHostAddress(), messagingNodeListener.getLocalPort());
		registrySender.send(ev.getBytes());
		System.out.println("Sending deregister request");
	}
	
	private synchronized void exit() throws IOException
	{
		for(Socket s : connections.values())
			s.close();
		registryConnectionReceiver.close();
		messagingNodeListener.close();
	}
	
	@Override
	public void onEvent(Event ev) {
		
		switch(ev.getType())
		{
		case REGISTER_RESPONSE:
			System.out.println(((RegisterResponse) ev).getInfo());
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
			break;
		default:
			break;
		}
	}
	
	private void onEvent(Message ev)
	{
		String destination = ev.getDestination();
		String ownAddress = registryConnection.getInetAddress().getHostAddress() + ":" + messagingNodeListener.getLocalPort();
		if (destination == ownAddress)
		{
			receiveSummation += ev.getPayload();
			receiveTracker++;
			return;
		}
		else
		{
			try
			{
				TCPSender t = new TCPSender(routingEntries.get(destination));
				t.send(ev.getBytes());
				relayTracker++;
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
				System.exit(0);
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
			Socket s = routingEntries.get(overlayNodes.get(randomKey));
			
			for (int j=0; j<numMsgPerRound; j++)
			{
				int payload = ThreadLocalRandom.current().nextInt();
				// Send to sink node
				try
				{
					TCPSender t = new TCPSender(s);
					t.send(new Message(new InetSocketAddress(s.getInetAddress().getHostAddress(), s.getPort()), payload).getBytes());
					sendSummation += payload;
				}
				catch(IOException e)
				{
					System.out.println(e.getMessage());
					System.exit(0);
				}
			}
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
				mapHostNameToInt.put(k1, nodeCount++);
				mapIntToHostName.add(k1);
			}
			
			String k2 = l.getAddressB().getHostString() + ":" + l.getAddressB().getPort();

			//System.out.println(k2);
			if(!mapHostNameToInt.containsKey(k2))
			{
				//System.out.println("done");
				mapHostNameToInt.put(k2, nodeCount++);
				mapIntToHostName.add(k2);
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
			
			graph[i][j] = l.getWeight();
			
		}
		
		// So now our graph consists of only infinity values and weights in case there is a link
		
		// Distances to infinity
		Integer distance[] = new Integer[nodeCount];
		
		// Previous to Undefined
		int prev[] = new int[nodeCount];
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
		boolean visited[] = new boolean[nodeCount];
		
		while(vertices.size() > 0)
		{
			// Pick up the node with minimum distance
			// will be source node at start
			
			int minDistance=infinity;
			int u=0;
			for(int t=0; t<nodeCount; t++)
			{
				if(!visited[t] && distance[t] < minDistance)
				{
					minDistance = distance[t];
					u = t;
				}
			}
			
			vertices.remove(u);
			visited[u] = true;
			
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
		
		for (int i = 0; i<nodeCount; i++)
		{
			int j = prev[i];
			while(j != undefined)
			{
				if (prev[j] == undefined)
					break;
				j = prev[j];
			}
			if (j != undefined)
			{
				synchronized(connections)
				{
					// a packet sent to i should be sent to j by this messaging node	
					routingEntries.put(mapIntToHostName.get(i), connections.get(mapIntToHostName.get(j)));
				}
					overlayNodes.add(mapIntToHostName.get(i));
			}
		}
		
		for(Map.Entry<String,Socket> s : routingEntries.entrySet())
		{
			if (s.getValue() == null)
				System.out.println(s.getKey() + "----");
			else
				System.out.println(s.getKey() + "----" + s.getValue().getInetAddress().getHostAddress() + ":" + s.getValue().getPort());
		}
		System.out.println("----");
		synchronized(connections) {
		for(Map.Entry<String,Socket> s : connections.entrySet())
		{
			if (s.getValue() == null)
				System.out.println(s.getKey() + "----");
			else
				System.out.println(s.getKey() + "----" + s.getValue().getInetAddress().getHostAddress() + ":" + s.getValue().getPort());
		}
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
				synchronized(connections)
				{
					connections.put(a.getHostString() + ":" + a.getPort(), s);
				}
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
					System.out.println(e.getMessage());
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
			System.out.println("Listening at " + super.sock.getInetAddress().getHostAddress() + ":" + super.sock.getLocalPort());
		}
		
		public void handleClient(Socket s)
		{
			// Add the received connection to send messages later
			synchronized(connections)
			{
				connections.put(registryConnection.getInetAddress().getHostAddress() + ":" + super.sock.getLocalPort(), s);
			}
			
			Thread t = new Thread(new MessagingNodeReceiver(s));
			t.setName("Messaging node " + super.sock.toString());
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
		
		public void handleEvent(EventType evType)
		{
			Event ev = null;
			try
			{
				switch(evType)
				{
				case REGISTER_RESPONSE:
					ev = readEventRegisterResponse();
					break;
				case MESSAGING_NODES_LIST:
					ev = readEventMessagingNodesList();
					break;
				case LINK_WEIGHTS_LIST:
					ev = readEventLinkWeightsList();
					break;
				case TASK_INITIATE:
					ev = readEventTaskInitiate();
					break;
				case MESSAGE:
					ev = readEventMessage();
					break;
				case PULL_TRAFFIC_SUMMARY:
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
			
			if (ev != null)
				onEvent(ev);
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
			return handleSingleWordCommands(words);
		}
		
		private boolean handleExitOverlay(String[] words)
		{
			boolean isValid = handleSingleWordCommands(words);
			
			if (isValid)
			{
				try
				{
					sendEventDeregisterRequest();
					exit();
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
					System.out.println("Unknown command: " + words[0]);
					isValid = false;
					break;
				}
			}
			
			return isValid;
		}
	}
}
