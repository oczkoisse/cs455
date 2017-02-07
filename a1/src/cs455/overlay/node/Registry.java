package cs455.overlay.node;

import cs455.overlay.util.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Registry implements Node {
	
	private static int portnum;
	
	private static final Registry instance = new Registry(Registry.portnum);
	
	private HashMap<String, Socket> registerdNodes = new HashMap<String, Socket>();
	
	private final RegistryListener registryListener = new RegistryListener(Registry.portnum);
	private final RegistryInterpreter registryInterpreter = new RegistryInterpreter(">> ", 0);
	
	private Registry(int portnum)
	{
		Registry.portnum = portnum;

		Thread t = new Thread(registryListener);
		t.setName("Registry Listener");
		t.start();
	}
	
	public static Registry getInstance()
	{
			return instance;
	}
	
	public static void init(int portnum)
	{
		Registry.portnum = portnum;
	}
	
	@Override
	public void onEvent(Event ev) {
		switch(ev.getType())
		{
		case REGISTER_REQUEST:
			onEvent((RegisterRequest) ev);
			break;
		case DEREGISTER_REQUEST:
			onEvent((DeregisterRequest) ev);
			break;
		default:
			break;
		}

	}
	
	private void onEvent(RegisterRequest ev)
	{
		
	}
	
	
	public static void main(String[] args)
	{
		if (args.length == 1)
		{
			int portnum = Integer.parseInt(args[0]);
			Registry.init(portnum);
			
			
			Registry.getInstance().registryInterpreter.run();
			
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
					break;
				case DEREGISTER_REQUEST:
					ev = readEventDeregisterRequest();
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
				System.exit(0);
			}
			
			if (ev != null)
				Registry.getInstance().onEvent(ev);
		}
	}

	private class RegistryInterpreter extends Interpreter
	{
		
		public RegistryInterpreter(String prompt, int maxTries)
		{
			super(prompt, maxTries);
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
