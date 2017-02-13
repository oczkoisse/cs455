package cs455.overlay.node;

import cs455.overlay.util.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class Registry implements Node {
	
	private static int portnum;
	private static Registry instance;
	
	private HashMap<Socket, InetSocketAddress> registeredNodes = new HashMap<Socket, InetSocketAddress>();
	
	private static RegistryListener registryListener;
	
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
			break;
		case TASK_COMPLETE:
			break;
		default:
			System.out.println("Unindentified message format");
			break;
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
		synchronized(registeredNodes)
		{
			if(registeredNodes.containsKey(s))
			{
				if(s.getInetAddress().getHostAddress().equals(ev.getIpAddress()))
				{
					registeredNodes.remove(s);
					msg = "Deregistration successfull. Currently connected node count is " + registeredNodes.size();
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
		System.out.println(msg);
	}
	
	private void setupOverlay(int numCons)
	{
		// Don't let any registration to proceed while setting up overlay
		synchronized(registeredNodes)
		{
			int nodeCount = registeredNodes.size();
			// All False array
			boolean[][] overlay = new boolean[nodeCount][nodeCount];
			
			// Neighborhood for each node
			int nbrd = numCons / 2;
			
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
					
					overlay[j][p] = overlay[p][j] = true;
					overlay[j][q] = overlay[q][j] = true;
					
					if (numCons % 2 != 0 && nodeCount % 2 == 0)
					{
						int r = (j + (nodeCount / 2)) % nodeCount;
						overlay[j][r] = overlay[r][j] = true;
					}
				}
			}
			
			ArrayList<Socket> msgNodes = new ArrayList<Socket>(registeredNodes.keySet());
			// Now use the overlay matrix to send link info to nodes
			for (int i=0; i < nodeCount; i++)
			{	
				MessagingNodesList ml = new MessagingNodesList();
				// Tell message node i
				TCPSender t = new TCPSender(msgNodes.get(i));
				for (int j = i+1; j < nodeCount; j++)
				{
					if(overlay[i][j])
					{
						// to connect to these message nodes
						ml.add(registeredNodes.get(msgNodes.get(j)).getHostString(), registeredNodes.get(msgNodes.get(j)).getPort());
					}
				}
				
				try
				{
					t.send(ml.getBytes());
				}
				catch(IOException e)
				{
					System.out.println("Can't send bytes");
				}
				
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
					//ev = readEventTrafficSummary();
					//onEvent((TrafficSummary) ev);
					break;
				case TASK_COMPLETE:
					//ev = readEventTaskComplete();
					//onEvent((TaskComplete) ev);
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
				synchronized(registeredNodes)
				{
					for(Socket s: registeredNodes.keySet())	
					{
						System.out.println(s.toString());
					}
				}
			}
			
			return isValid;
		}
		
		private boolean handleListWeights(String[] words)
		{
			return handleSingleWordCommands(words);
		}
		
		private boolean handleSetupOverlay(String[] words)
		{
			boolean isValid = true;
			
			if (words.length == 2)
			{
				try
				{
					int numCon = Integer.parseInt(words[1]);
					synchronized(registeredNodes)
					{
						if (numCon < 1 || numCon >= registeredNodes.size())
							isValid = false;
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
					// Registry should take over from here
				}
				catch(NumberFormatException e)
				{
					isValid = false;
				}
			}
			else 
				return false;
			
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
				case "list-weights":
					isValid = handleListWeights(words);
					break;
				case "setup-overlay":
					isValid = handleSetupOverlay(words);
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
}
