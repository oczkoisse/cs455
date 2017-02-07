package cs455.overlay.node;

import cs455.overlay.util.Interpreter;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;

import java.io.*;
import java.net.*;

public class MessagingNode implements Node {

	private String registryIp;
	private int registryPort;
	private Socket registryConnection;
	
	private boolean connectToRegistry()
	{
		boolean success = false;
		try
		{
			registryConnection = new Socket(registryIp, registryPort);
			success = true;
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
		}
		return success;
	}
	
	@Override
	public void onEvent(Event ev) {

	}
	
	public MessagingNode(String registryIp, int registryPort)
	{
		this.registryIp = registryIp;
		this.registryPort = registryPort;
		
		if(connectToRegistry())
		{
			System.out.println("Connected to Registry");
		}
		else
		{
			System.out.println("Unable to connect to Registry");
			System.exit(0);
		}
	}
	
	public static void main(String[] args)
	{
		if (args.length == 2)
		{
			String registryIp = args[0];
			int registryPort = Integer.parseInt(args[1]);
			
			// Initialize this messaging node
			MessagingNode m = new MessagingNode(registryIp, registryPort);
			
			// Weird syntax to initialize the nested class from the enclosing class object
			MessagingNodeInterpreter mp = m.new MessagingNodeInterpreter(">> ", 3);
			MessagingNodeListener ml = m.new MessagingNodeListener();
			
			ml.run();
			mp.run();
			
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
		}
		
		public void handleClient(Socket s)
		{
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
			byte status = super.din.readByte();
			String additionalInfo = super.din.readUTF();
			
			return new RegisterResponse(status, additionalInfo);
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
					break;
				case LINK_WEIGHTS:
					break;
				case TASK_INITIATE:
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
		public MessagingNodeInterpreter(String prompt, int maxTries)
		{
			super(prompt, maxTries);
		}
		
		private boolean handlePrintShortestPath()
		{
			// Messaging node should take over from here
			return true;
		}
		
		private boolean handleExitOverlay()
		{
			// Messaging node should take over from here
			return true;
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
					isValid = this.handlePrintShortestPath();
					break;
				case "exit-overlay":
					isValid = this.handleExitOverlay();
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
