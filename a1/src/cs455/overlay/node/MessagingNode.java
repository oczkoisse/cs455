package cs455.overlay.node;

import cs455.overlay.util.Interpreter;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class MessagingNode implements Node {

	private String registryIp;
	private int registryPort;
	private Socket registryConnection;
	private TCPSender registrySender;
	private HashMap<String, Socket> connections = new HashMap<String, Socket>();
	
	private MessagingNodeListener messagingNodeListener;
	private MessagingNodeReceiver registryConnectionReceiver;
	
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
	
	private void exit() throws IOException
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
			System.out.println("Got messaging nodes list");
			onEvent((MessagingNodesList) ev);
			break;
		case LINK_WEIGHTS_LIST:
			System.out.println("Got link weights");
			onEvent((LinkWeightsList) ev);
		case TASK_INITIATE:
			break;
		case PULL_TRAFFIC_SUMMARY:
			break;
		default:
			break;
		}
	}
	
	private void onEvent(LinkWeightsList ev)
	{
		
	}
	
	private void onEvent(MessagingNodesList ev)
	{
		for(InetSocketAddress a: ev)
		{
			try
			{
				Socket s = new Socket(a.getHostString(), a.getPort());
				connections.put(s.getInetAddress().getHostAddress() + ":" + s.getPort(), s);
				System.out.println("Connected successfully to " + a.getHostString() + ":" + a.getPort());
			}
			catch(IOException e)
			{
				System.out.println("Can't connect to " + a.getHostString() + ":" + a.getPort());
			}
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
			System.out.println("Listening at " + super.sock.getInetAddress().getHostAddress() + ":" + super.sock.getLocalPort());
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
			
			LinkWeightsList.LinkInfo linfo = l.new LinkInfo(ipAddressA, portnumA, ipAddressB, portnumB, weight);
			
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
