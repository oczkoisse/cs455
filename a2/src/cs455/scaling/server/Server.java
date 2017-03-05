package cs455.scaling.server;

import cs455.scaling.works.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Server implements Runnable {

	private static int portnum;
	private static int poolSize;
	private ThreadPoolManager tpm;
	private static Server serverInstance;
	private static ServerSocketChannel serverChannel;
	private static Selector selector;
	
	
	private ArrayList<Work> pendingWorks = new ArrayList<Work>();
	private HashMap<SelectionKey, ByteBuffer> pendingWrites = new HashMap<SelectionKey, ByteBuffer>();
	
	private volatile int activeConnections;
	private Integer messagesProcessed;
	
	private Server(int portnum, int poolSize)
	{
		Server.portnum = portnum;
		Server.poolSize = poolSize;
		this.tpm = new ThreadPoolManager(Server.poolSize);
		
		this.activeConnections = 0;
		this.messagesProcessed = 0;
	}
	
	private class Summary extends TimerTask
	{
		public void run()
		{
			synchronized(System.out)
			{
				double rate;
				synchronized(messagesProcessed)
				{
					rate = messagesProcessed / 5.0;
					messagesProcessed = 0;
				}
				System.out.println("[" + super.scheduledExecutionTime() + "] " 		+
						   "Current Server Throughput: "	 						+
							rate + " messages/s, "						 			+
						   "Active Client Connections: " 							+
							activeConnections);
			}
		}
	}
	
	public static void init(int portnum, int poolSize)
	{
		if(serverInstance == null)
		{
			serverInstance = new Server(portnum, poolSize);
			
			// Arrange to listen at this port number
			try
			{
				Server.selector = Selector.open();
				
				Server.serverChannel = ServerSocketChannel.open();
				serverChannel.configureBlocking(false);
				serverChannel.bind(new InetSocketAddress("0.0.0.0", Server.portnum));
				serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
				System.exit(0);
			}
			
		}
		else
			throw new IllegalStateException("Repeated initialization");
	}
	
	public static Server getInstance()
	{
		if(serverInstance != null)
		{
			return serverInstance;
		}
		else
			throw new IllegalStateException("Server instance is not initialized");
	}
	
	public void addWork(Work w)
	{
		synchronized(pendingWorks)
		{
			pendingWorks.add(w);
			System.out.println("Pending works count: " + pendingWorks.size());
		}
		Server.selector.wakeup();
	}
	
	private void completePendingWorks()
	{
		synchronized(pendingWorks)
		{
			Iterator<Work> pw = pendingWorks.iterator();
			while(pw.hasNext())
			{
				Work w = pw.next();
				
				switch(w.getType())
				{
				case READ: 
					w.getSelectionKey().interestOps(SelectionKey.OP_READ);
					break;
				case WRITE:
					pendingWrites.put(w.getSelectionKey(), ((WriteWork) w).getData());
					break;
				case HASH: 
					tpm.addWork(w);
					break;
				case DEREGISTER: 
					try
					{
						w.getSelectionKey().channel().close();
					}
					catch(IOException e)
					{
						System.out.println(e.getMessage());
						System.exit(0);
					}
					w.getSelectionKey().cancel();
					break;
				}
				
				pw.remove();
			}
		}
	}
	
	public void run()
	{
		new Thread(this.tpm).start();
		
		Timer t = new Timer();
		t.scheduleAtFixedRate(new Summary(), new Date(), 5000);
		
		while (true)
		{
			// apply updates
			completePendingWorks();
			
			// call select
			try
			{
				Server.selector.select();
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
			}
			
			Iterator<SelectionKey> selectedKeys = Server.selector.selectedKeys().iterator();
			
			try
			{
				while(selectedKeys.hasNext())
				{
					SelectionKey selKey = selectedKeys.next();
					
					if(selKey.isValid())
					{
						if(selKey.isAcceptable())
						{
							ServerSocketChannel serv = (ServerSocketChannel) (selKey.channel());
							SocketChannel client = serv.accept();
							if (client != null)
							{
								client.configureBlocking(false);
								client.register(Server.selector, SelectionKey.OP_READ);
								activeConnections++;
							}
						}
						else 
						{
							if(selKey.isReadable())
							{
								tpm.addWork(new ReadWork(selKey));
								selectedKeys.remove();
							}
							if(selKey.isWritable())
							{
								if(pendingWrites.containsKey(selKey))
								{
									tpm.addWork(new WriteWork(selKey, pendingWrites.get(selKey)));
									pendingWrites.remove(selKey);
									selectedKeys.remove();
								}
							}
						}
					}
				}
			}
			catch(ClosedChannelException e)
			{
				System.out.println(e.getMessage());
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
			}
			
		}
	}
	
	public static void main(String[] args)
	{
		int portnum, poolSize;
		
		if(args.length == 2)
		{
			try
			{
				portnum = Integer.parseInt(args[0]);
				poolSize = Integer.parseInt(args[1]);
				Server.init(portnum, poolSize);
			}
			catch(NumberFormatException e)
			{
				System.out.println("Usage: cs455.scaling.server.Server <portnum> <thread-pool-size");
				System.exit(0);
			}
			
			new Thread(Server.getInstance()).start();
			
		}
		else
		{
			System.out.println("Usage: cs455.scaling.server.Server <portnum> <thread-pool-size");
			System.exit(0);
		}
	}
}
