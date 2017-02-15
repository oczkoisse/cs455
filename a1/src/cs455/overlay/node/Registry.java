package cs455.overlay.node;
import java.util.concurrent.TimeUnit;
import cs455.overlay.util.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;
import java.util.concurrent.ThreadLocalRandom;

import java.io.*;
import java.net.*;
import java.util.*;

public class Registry implements Node {
	
	private static int portnum;
	private static Registry instance;
	
	private HashMap<Socket, InetSocketAddress> registeredNodes = new HashMap<Socket, InetSocketAddress>();
	private HashSet<InetSocketAddress> taskCompleteNotReceived;
	private HashSet<InetSocketAddress> summaryNotReceived;
	private HashMap<InetSocketAddress, TrafficSummary> trafficSummaries;
	
	private static RegistryListener registryListener;
	
	private Overlay ov;
	
	
	private Registry(int portnum)
	{
		Registry.portnum = portnum;
	}
	
	public static void init(int portnum)
	{
		if (instance == null)
		{
			Registry.instance = new Registry(portnum);
			Registry.registryListener = Registry.instance.new RegistryListener(Registry.portnum);
			Thread t  = new Thread(Registry.registryListener);
			t.setName("Registry Listener");
			t.start();
		}
	}
	
	public static Registry getInstance()
	{
		return instance;
	}

	
	@Override
	public void onEvent(Event ev) {
		switch(ev.getType())
		{
		case TRAFFIC_SUMMARY:
			onEvent((TrafficSummary) ev);
			break;
		case TASK_COMPLETE:
			onEvent((TaskComplete) ev);
			break;
		default:
			System.out.println("Unidentified message format");
			break;
		}

	}
	
	private void onEvent(TrafficSummary ev)
	{
		InetSocketAddress addr = ev.getAddress();
		
		synchronized(summaryNotReceived)
		{
			synchronized(trafficSummaries)
			{
				if(summaryNotReceived.remove(addr))
					trafficSummaries.put(addr, ev);
				
				if (summaryNotReceived.size() == 0)
				{
					// Print combined summaries
					final String space = "   ";
					long sentCount = 0, receivedCount = 0, sentSummation = 0, receivedSummation = 0;
					for(Map.Entry<InetSocketAddress, TrafficSummary> entry: trafficSummaries.entrySet())
					{
						InetSocketAddress a = entry.getKey();
						TrafficSummary ts = entry.getValue();
						System.out.println(a.toString() + space + ts.getSentCount() + space + ts.getReceivedCount() + space + ts.getSentSummation() + space + ts.getReceivedSummation() + space + ts.getRelayCount());
						sentCount += ts.getSentCount();
						receivedCount += ts.getReceivedCount();
						sentSummation += ts.getSentSummation();
						receivedSummation += ts.getReceivedSummation();
					}
					
					System.out.println(space + space + space + sentCount + space + receivedCount + space + sentSummation + space + receivedSummation);
				}
			}
		}
	}
	
	private void onEvent(TaskComplete ev)
	{
		InetSocketAddress a = ev.getAddress();
		
		synchronized(taskCompleteNotReceived)
		{
			//System.out.println("Got the lock");
			//System.out.println(a.toString());
			taskCompleteNotReceived.remove(a);
			//System.out.println(taskCompleteNotReceived.size());
			//System.out.println(taskCompleteNotReceived.toString());
		
			// Check if all TaskComplete messages have been received
			if (taskCompleteNotReceived.size() == 0)
			{
				System.out.println("All task completion messages received. Waiting...");
				try
				{
					// If so, then wait for 20s
					TimeUnit.SECONDS.sleep(20);
				}
				catch(InterruptedException e)
				{
					System.out.println(e.getMessage());
				}
				finally{
					synchronized(registeredNodes)
					{
						for(Socket s: registeredNodes.keySet())
						{
							// Issue PULL_TRAFFIC_SUMMARY
							TCPSender t = new TCPSender(s);
							try
							{
								t.send(new PullTrafficSummary().getBytes());
							}
							catch(IOException e)
							{
								System.out.println(e.getMessage());
							}
						}
					}
				}
			}
		}
	}
	
	
	private void onEvent(RegisterRequest ev, Socket s)
	{	
		String msg = null;
		boolean status = false;
		synchronized(registeredNodes)
		{
			if(!registeredNodes.containsKey(s))
			{
				if(s.getInetAddress().getHostAddress().equals(ev.getIpAddress()))
				{
					// Need to put only the connection info for the listener on each 
					registeredNodes.put(s, new InetSocketAddress(ev.getIpAddress(), ev.getPort()));
					msg = "Registration successfull. Currently connected node count is " + registeredNodes.size();
					status = true;
				}
				else
				{
					msg = "Registration unsuccessfull because of IP address mismatch between " 
						+ s.getInetAddress().getHostAddress() + " and "
						+ ev.getIpAddress();
				}
			}
			else
			{
				msg = "Registration unsuccessfull because node is already registered";
			}
		}
		try
		{
			TCPSender t = new TCPSender(s);
			RegisterResponse evr = new RegisterResponse(status, msg);
			t.send(evr.getBytes());
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}
	
	
	private void onEvent(DeregisterRequest ev, Socket s)
	{
		String msg = null;
		boolean status = false;
		synchronized(registeredNodes)
		{
			if(registeredNodes.containsKey(s))
			{
				if(s.getInetAddress().getHostAddress().equals(ev.getIpAddress()))
				{
					registeredNodes.remove(s);
					msg = "Deregistration successfull. Currently connected node count is " + registeredNodes.size();
					status = true;
				}
				else
				{
					msg = "Deregistration unsuccessfull because of IP address mismatch";
				}
			}
			else
			{
				msg = "Deregistration unsuccessfull since node is not even registered";
			}
		}
		try
		{
			TCPSender t = new TCPSender(s);
			DeregisterResponse evr = new DeregisterResponse(status, msg);
			t.send(evr.getBytes());
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}
	
	private void setupOverlay(int numCons)
	{
		// Don't let any registration to proceed while setting up overlay
		synchronized(registeredNodes)
		{
			// First set up record for noting reception of TASK_COMPLETE		
			taskCompleteNotReceived = new HashSet<InetSocketAddress>(registeredNodes.values());
			summaryNotReceived = new HashSet<InetSocketAddress>(registeredNodes.values());
			trafficSummaries = new HashMap<InetSocketAddress, TrafficSummary>();
			
			try
			{
				this.ov = new Overlay(numCons);
			}
			catch(IllegalArgumentException e)
			{
				System.out.println(e.getMessage());
				this.ov = null;
				return;
			}
			for (Map.Entry<Socket, MessagingNodesList> entry : ov.getMessagingNodesList().entrySet()) {
			    Socket s = entry.getKey();
			    MessagingNodesList m = entry.getValue();
			    
			    try
			    {
			    	TCPSender t = new TCPSender(s);
				    t.send(m.getBytes());
			    }
			    catch(IOException e)
			    {
			    	System.out.println("Unable to send messaging nodes list to " + s.toString());
			    }
			    
			}
		}
	}
	
	
	
	private void sendLinkWeights()
	{
		if (ov != null)
		{
			LinkWeightsList l = ov.getLinkWeightsList();
			synchronized(registeredNodes)
			{
				for(Socket s: registeredNodes.keySet())
				{
					try
					{
						TCPSender t = new TCPSender(s);
						t.send(l.getBytes());
					}
					catch(IOException e)
					{
						System.out.println("Unable to send link weights list " + s.toString());
					}
				}
			}
		}
	}
	
	private void listWeights()
	{
		if (ov != null)
		{
			LinkWeightsList l = ov.getLinkWeightsList();
			for(LinkWeightsList.LinkInfo li : l)
			{
				System.out.println(li.getAddressA().toString() + " -- " + li.getWeight() + " -- " + li.getAddressB().toString());
			}
		}
		
	}
	
	private void listMessagingNodes()
	{
		synchronized(registeredNodes)
		{
			for(InetSocketAddress a: registeredNodes.values())	
			{
				System.out.println(a.toString());
			}
		}
	}
	
	private void start(int numRounds)
	{
		for(Socket s: registeredNodes.keySet())
		{
			TCPSender t = new TCPSender(s);
			try
			{
				t.send(new TaskInitiate(numRounds).getBytes());
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
			}
		}
	}
	
	
	public static void main(String[] args)
	{
		if (args.length == 1)
		{
			int portnum = Integer.parseInt(args[0]);
			
			Registry.init(portnum);
			
			Registry.getInstance().new RegistryInterpreter().run();
			// Dead code follows
		}
		else
		{
			System.out.println("Usage: java cs455.overlay.node.Registry <portnum>");
			System.exit(0);
		}
	}
	
	private class RegistryListener extends TCPListenerThread 
	{	
		public RegistryListener(int port)
		{
			super(port);
		}
		
		public void handleClient(Socket s)
		{
			Thread t = new Thread(new RegistryReceiver(s));
			t.setName("Registry receiver - " + s.toString());
			t.start();
		}
	}
	
	private class RegistryReceiver extends TCPReceiverThread
	{
		public RegistryReceiver(Socket s)
		{	
			super(s);
		}
		
		private Event readEventRegisterRequest() throws IOException
		{
			String ip = super.din.readUTF();
			int port = super.din.readInt();			
			return new RegisterRequest(ip, port);
		}
		
		private Event readEventDeregisterRequest() throws IOException
		{
			String ip = super.din.readUTF();
			int port = super.din.readInt();
			return new DeregisterRequest(ip, port);
		}
		
		private Event readEventTaskComplete() throws IOException
		{
			String ip = super.din.readUTF();
			int port = super.din.readInt();
			return new TaskComplete(ip, port);
		}
		
		private Event readEventTrafficSummary() throws IOException
		{
			String ip = super.din.readUTF();
			int port = super.din.readInt();
			
			int sentCount = super.din.readInt();
			long sentSum = super.din.readLong();
			int receivedCount = super.din.readInt();
			long receivedSum = super.din.readLong();
			int relayedCount = super.din.readInt();

			TrafficSummary ev = new TrafficSummary(ip, port, sentCount, sentSum, receivedCount, receivedSum, relayedCount);
			return ev;
		}
		
		
		
		public void handleEvent(EventType evType)
		{
			Event ev = null;
			try
			{
				switch(evType)
				{
				case REGISTER_REQUEST:
					ev = readEventRegisterRequest();
					onEvent((RegisterRequest) ev, super.sock);
					break;
				case DEREGISTER_REQUEST:
					ev = readEventDeregisterRequest();
					onEvent((DeregisterRequest) ev, super.sock);
					break;
				case TRAFFIC_SUMMARY:
					ev = readEventTrafficSummary();
					onEvent(ev);
					break;
				case TASK_COMPLETE:
					ev = readEventTaskComplete();
					onEvent(ev);
					break;
				default:
					System.out.println("Unknown message format received by registry");
					System.exit(0);
				}
			}
			catch(IOException e)
			{
				System.out.println("Can't read event: " + e.getMessage());
			}
		}
	}

	private class RegistryInterpreter extends Interpreter
	{
		
		public RegistryInterpreter()
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
		
		private boolean handleListMessagingNodes(String[] words)
		{
			boolean isValid = handleSingleWordCommands(words);
			
			if (isValid)
			{	
				listMessagingNodes();
			}
			
			return isValid;
		}
		
		private boolean handleListWeights(String[] words)
		{
			boolean isValid = handleSingleWordCommands(words);
			if(isValid)
			{
				listWeights();
			}
			return isValid;
		}
		
		private boolean handleSetupOverlay(String[] words)
		{
			boolean isValid = true;
			
			if (words.length == 2)
			{
				try
				{
					int numCon;
					try
					{
						numCon = Integer.parseInt(words[1]);
					}
					catch(NumberFormatException e)
					{
						isValid = false;
						return isValid;
					}
					
					if (isValid)
						setupOverlay(numCon);
					
				}
				catch(NumberFormatException e)
				{
					isValid = false;
				}
			}
			else 
				isValid = false;
			
			if (!isValid)
				System.out.println("Usage: setup-overlay <number-of-connected-nodes>");
			
			return isValid;
		}
		
		private boolean handleSendOverlayLinkWeights(String[] words)
		{
			boolean isValid = handleSingleWordCommands(words);
			if (isValid)
			{
				sendLinkWeights();
			}
			return isValid;
		}
		
		private boolean handleStart(String[] words)
		{
			boolean isValid = true;
			
			if (words.length == 2)
			{
				try{
					int numRounds = Integer.parseInt(words[1]);
					
					start(numRounds);
					
				}
				catch(NumberFormatException e)
				{
					isValid = false;
				}
			}
			else 
				isValid = false;
			
			if (!isValid)
				System.out.println("Usage: start <number-of-rounds>");
			
			return isValid;
		}
		
		public boolean handleCommand(String cmd)
		{
			String[] words = cmd.trim().split("\\s+");
			
			// Note that initial whitespace creates an empty string at the beginning
			boolean isValid = true;
			
			if (words.length > 0 && words[0].length() > 0)
			{
				switch(words[0])
				{
				case "list-messaging-nodes":
					isValid = handleListMessagingNodes(words);
					break;
				case "setup-overlay":
					isValid = handleSetupOverlay(words);
					break;
				case "list-weights":
					isValid = handleListWeights(words);
					break;
				case "send-overlay-link-weights":
					isValid = handleSendOverlayLinkWeights(words);
					break;
				case "start":
					isValid = handleStart(words);
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
	
	private class Overlay
	{
		private int degree;
		private int nodeCount;
		
		// The sockets to which registry is connected to
		// Allows to be indexed by integers, and in a fixed order
		private final ArrayList<Socket> addresses;
		
		private int[][] overlay;
		
		public Overlay(int degree)
		{
			this.degree = degree;
			// Coupling with Registry
			synchronized(registeredNodes)
			{
				this.addresses = new ArrayList<Socket>(registeredNodes.keySet());
				this.nodeCount = this.addresses.size();
			}
			constructOverlay();
		}
		
		// Constructs overlay and link weights
		private void constructOverlay()
		{
			// All 0 array
			overlay = new int[nodeCount][nodeCount];
			
			// Neighborhood for each node
			int nbrd = degree / 2;
			

			if ((degree < 1 || degree >= nodeCount) || (nodeCount % 2 != 0 && degree % 2 != 0))
				throw new IllegalArgumentException("No overlay possible for current parameters: " + nodeCount + "," + degree);
			
			
			for (int i = 1; i <= nbrd; i++)
			{
				for (int j = 0; j < nodeCount; j++)
				{
					int p = (j + i) % nodeCount;
					// Modulus operator can return a negative answer in Java
					// so need to be careful
					int q = (j - i) % nodeCount;
					if (q < 0)
						q += nodeCount;
					
					// A random weight between 1 and 10 (both inclusive)
					int weight = ThreadLocalRandom.current().nextInt(1, 11);
					overlay[j][p] = overlay[p][j] = weight;
					
					weight = ThreadLocalRandom.current().nextInt(1, 11);
					overlay[j][q] = overlay[q][j] = weight;
					
					if (degree % 2 != 0 && nodeCount % 2 == 0)
					{
						int r = (j + (nodeCount / 2)) % nodeCount;
						weight = ThreadLocalRandom.current().nextInt(1, 11);
						overlay[j][r] = overlay[r][j] = weight;
					}
				}
			
			}
		}
		
		
		// Return MessagingNodesList indexed by Socket for which it is meant to be sent
		public HashMap<Socket, MessagingNodesList> getMessagingNodesList()
		{
			HashMap<Socket, MessagingNodesList> hl = new HashMap<Socket, MessagingNodesList>();
			synchronized(registeredNodes)
			{
				for(int i=0; i<nodeCount; i++)
				{
					MessagingNodesList l = new MessagingNodesList();
					for (int j=i+1; j<nodeCount; j++)
					{
						// If node j is a neighbor of node i
						if (overlay[i][j] > 0)
						{
							// Encapsulate the listening addresses of neighbor messaging nodes
							l.add(registeredNodes.get(addresses.get(j)).getHostString(), registeredNodes.get(addresses.get(j)).getPort());
						}
					}
					hl.put(addresses.get(i), l);
				}
			}
			return hl;
		}
		
		// Return the LinkWeightsList to be sent to all nodes
		public LinkWeightsList getLinkWeightsList()
		{
			LinkWeightsList l = new LinkWeightsList();
			
			if (registeredNodes != null)
			{
				synchronized(registeredNodes)
				{
					for(int i=0; i<nodeCount; i++)
					{
						for (int j=i+1; j<nodeCount; j++)
						{
							if (overlay[i][j] > 0)
							{
								l.add(l.new LinkInfo(new InetSocketAddress(registeredNodes.get(addresses.get(i)).getHostString(), // ip address of i
																		   registeredNodes.get(addresses.get(i)).getPort()), // port at which i is listening
													 new InetSocketAddress(registeredNodes.get(addresses.get(j)).getHostString(), // ip address of j
														   				   registeredNodes.get(addresses.get(j)).getPort()), // port at which j is listening
													 overlay[i][j]));
							}
						}
					}
				}
			}
			
			return l;
		}
		
	}
}
