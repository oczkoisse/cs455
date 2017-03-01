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
	
	private ArrayList<Work> pendingWorks = new ArrayList<Work>();
	private HashMap<SelectionKey, ByteBuffer> pendingWrites = new HashMap<SelectionKey, ByteBuffer>();
	private ThreadPoolManager tpm;
	
	private static Server serverInstance;
	
	private static ServerSocketChannel serverChannel;
	private static Selector selector;
	
	private Server(int portnum, int poolSize)
	{
		Server.portnum = portnum;
		Server.poolSize = poolSize;
		this.tpm = new ThreadPoolManager(Server.poolSize);
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
		}
		Server.selector.wakeup();
	}
	
	public void run()
	{
		new Thread(this.tpm).start();
		
		while (true)
		{
			// apply updates
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
						w.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
						pendingWrites.put(w.getSelectionKey(), ((WriteWork) w).getData());
						break;
					case HASH: 
						tpm.addWork(w);
						break;
					case DEREGISTER: 
						w.getSelectionKey().cancel();
						
						try
						{
							w.getSelectionKey().channel().close();
						}
						catch(IOException e)
						{
							System.out.println(e.getMessage());
							System.exit(0);
						}
						break;
					}
					
					pw.remove();
				}
			}
			
			// call select
			try
			{
				System.out.println("Selecting");
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
							}
							
						}
						else if(selKey.isReadable())
						{
							tpm.addWork(new ReadWork(selKey));
							selectedKeys.remove();
						}
						else if(selKey.isWritable())
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
