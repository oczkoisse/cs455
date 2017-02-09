package cs455.overlay.node;

import cs455.overlay.util.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Registry implements Node {
	
	private static int portnum;
	private static Registry instance;
	
	private HashMap<String, Socket> registeredNodes = new HashMap<String, Socket>();
	
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
		case REGISTER_REQUEST:
			break;
		case DEREGISTER_REQUEST: 
			onEvent((DeregisterRequest) ev);
			break;
		default:
			break;
		}

	}
	
	private String getKey(RegisterRequest ev)
	{
		return ev.getIpAddress() + ":" + ev.getPort();
	}
	
	private void handleRegistration(RegisterRequest ev, Socket s)
	{	
		String key = getKey(ev);
		String msg = null;
		boolean status = false;
		synchronized(registeredNodes)
		{
			if(!registeredNodes.containsKey(key))
			{
				if(s.getInetAddress().getHostAddress().equals(ev.getIpAddress()) && s.getPort() == ev.getPort() )
				{
					registeredNodes.put(key, s);
					msg = "Registration successfull. Currently connected node count is " + registeredNodes.size();
					status = true;
				}
				else
				{
					msg = "Registration unsuccessfull because of IP address mismatch";
				}
			}
			else
			{
				msg = "Registration unsuccessfull because node is already registered";
			}
		}
		System.out.println(msg);
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
	
	private void onEvent(DeregisterRequest ev)
	{
		
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
					handleRegistration((RegisterRequest) ev, super.sock);
					break;
				case DEREGISTER_REQUEST:
					ev = readEventDeregisterRequest();
					Registry.getInstance().onEvent(ev);
					break;
				case TRAFFIC_SUMMARY:
				case TASK_COMPLETE:
					System.out.println("Event unimplemented");
					break;
				default:
					System.out.println("Unknown event encountered by registry");
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
				System.out.println("Usage: " + words[0].trim());
			
			return isValid;
		}
		
		private boolean handleListMessagingNodes(String[] words)
		{
			return this.handleSingleWordCommands(words);
		}
		
		private boolean handleListWeights(String[] words)
		{
			return this.handleSingleWordCommands(words);
		}
		
		private boolean handleSetupOverlay(String[] words)
		{
			boolean isValid = true;
			
			if (words.length == 2)
			{
				try{
					int numCon = Integer.parseInt(words[1]);
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
				System.out.println("Usage: setup-overlay <number-of-connected-nodes>");
			
			return isValid;
		}
		
		private boolean handleSendOverlayLinkWeights(String[] words)
		{
			return this.handleSingleWordCommands(words);
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
				switch(words[0].trim())
				{
				case "list-messaging-nodes":
					isValid = this.handleListMessagingNodes(words);
					break;
				case "list-weights":
					isValid = this.handleListWeights(words);
					break;
				case "setup-overlay":
					isValid = this.handleSetupOverlay(words);
					break;
				case "send-overlay-link-weights":
					isValid = this.handleSendOverlayLinkWeights(words);
					break;
				case "start":
					isValid = this.handleStart(words);
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
