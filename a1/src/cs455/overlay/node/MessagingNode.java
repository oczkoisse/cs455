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
	private TCPSender registrySender;
	
	private MessagingNodeListener messagingNodeListener;
	
	public boolean connectToRegistry()
	{
		boolean success = false;
		try
		{
			registryConnection = new Socket(registryIp, registryPort);
			registrySender = new TCPSender(registryConnection);
			success = true;
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
		
		return success;
	}
	
	public void sendEventRegisterRequest() throws IOException
	{
		RegisterRequest ev = new RegisterRequest(registryConnection.getLocalAddress().getHostAddress(), registryConnection.getLocalPort());
		registrySender.send(ev.getBytes());
		System.out.println("Sending register request");
	}
	
	private void sendEventDeregisterRequest() throws IOException
	{
		DeregisterRequest ev = new DeregisterRequest(registryConnection.getLocalAddress().getHostAddress(), registryConnection.getLocalPort());
		registrySender.send(ev.getBytes());
		System.out.println("Sending deregister request");
	}
	
	@Override
	public void onEvent(Event ev) {
		
		switch(ev.getType())
		{
		case REGISTER_RESPONSE:
			System.out.println(((RegisterResponse) ev).getInfo());
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
			break;
		}
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
			boolean status = super.din.readByte() == 1 ? true : false;
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
