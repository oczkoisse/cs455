package cs455.scaling.server;

import cs455.scaling.works.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

/**
 * Server that accepts clients to receive fixed-size messages at a fixed rate,
 * hashes those messages, and sends the hashes back to the clients as acknowledgement
 * It does so by employing a thread pool to manage read/hash/write operations
 * while relying on java non-blocking i/o to handle multiple clients in a single thread.
 * Server can be run in its own thread.
 * 
 * @author Rahul Bangar
 *
 */
public class Server implements Runnable {

	/**
	 *  The port number at which to listen for communications
	 */
	private static int portnum;
	
	/**
	 * The size of the thread pool to be used
	 */
	private static int poolSize;
	
	/**
	 * Internal reference to the corresponding Thread Pool Manager
	 */
	private final ThreadPoolManager tpm;
	
	/**
	 * Single instance of the server
	 */
	private static Server serverInstance;
	
	/**
	 * The channel at which to listen for new communications
	 */
	private static ServerSocketChannel serverChannel;
	
	/**
	 * One and only selector for handling all client connections
	 */
	private static Selector selector;
	
	
	/**
	 * A list of works  that need to be done
	 */
	private ArrayList<Work> pendingWorks = new ArrayList<Work>();
	
	/**
	 * A list of pending writes as sifted from the pending works.
	 * Note that each selection key could have more than one pending writes.
	 */
	private HashMap<SelectionKey, ArrayList<ByteBuffer>> pendingWrites = new HashMap<SelectionKey, ArrayList<ByteBuffer>>();
	
	/**
	 * The number of active connections being served by the server
	 */
	private volatile int activeConnections;
	
	/**
	 * Number of messages processed since last summary printing
	 */
	private Integer messagesProcessed;
	
	/**
	 * Instantiates the Server singleton.
	 * Accepts connections on ANY interface.
	 * 
	 * @param portnum	the port number to use for accepting connections
	 * @param poolSize	the thread pool size to be used for managing communications
	 */
	private Server(int portnum, int poolSize)
	{
		Server.portnum = portnum;
		Server.poolSize = poolSize;
		this.tpm = new ThreadPoolManager(Server.poolSize);
		
		this.activeConnections = 0;
		this.messagesProcessed = 0;
	}
	
	/**
	 * A TimerTask implementation to be used with Timer.
	 * Prints summary of server communications in the form of:
	 * [time stamp] Current Server Throughput: x messages/s Active Client Connections: y 
	 * 
	 */
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
	
	/**
	 * Initializes the Server singleton.
	 * The server so initialized accepts connections at ANY interface.
	 * 
	 * @param portnum	the port number at which to listen for incoming connections
	 * @param poolSize	the size of thread pool used for managing communications
	 */
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
	
	/**
	 * Gets the server instance that was previously initialized using {@link cs455.scaling.server.Server#init(int, int)}
	 * 
	 * @return	the server instance
	 */
	public static Server getInstance()
	{
		if(serverInstance != null)
		{
			return serverInstance;
		}
		else
			throw new IllegalStateException("Server instance is not initialized");
	}
	
	/**
	 * Add a work to server's to-do list
	 * Immediately wakes up the server if listening for communications,
	 * so that it can get to the just added work
	 * 
	 * @param w	work to be done
	 */
	public void addWork(Work w)
	{
		synchronized(pendingWorks)
		{
			pendingWorks.add(w);
		}
		Server.selector.wakeup();
	}
	
	/**
	 * Notifies the server that a message was successfully processed.
	 * This has the side effect of incrementing the counter for processed 
	 * messages. A message is defined to be processed when it's hash is 
	 * completely written to the respective client
	 */
	void notifyMessageProcessed()
	{
		synchronized(messagesProcessed)
		{
			messagesProcessed++;
		}
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
				case WRITE:
					w.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
					if (pendingWrites.containsKey(w.getSelectionKey()))
					{
						pendingWrites.get(w.getSelectionKey()).add(((WriteWork) w).getData());
					}
					else
					{
						ArrayList<ByteBuffer> t = new ArrayList<ByteBuffer>();
						t.add(((WriteWork) w).getData());
						pendingWrites.put(w.getSelectionKey(), t);
					}
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
					activeConnections--;
					break;
				default:
					System.out.println("Rogue work detected");
					break;
				}
				
				pw.remove();
			}
		}
	}
	
	/**
	 * Server logic for incoming connections.
	 * Starts the associated thread pool manager, and
	 * schedules the printing of summary every 5 seconds.
	 * All the underlying read/write/hash work is dispatched to the
	 * associated thread pool manager.
	 */
	public void run()
	{
		new Thread(this.tpm).start();
		
		Timer t = new Timer();
		t.scheduleAtFixedRate(new Summary(), new Date(), 5000);
		
		while (true)
		{
			// apply updates
			completePendingWorks();
			
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
						else if(selKey.isReadable())
						{
							tpm.addWork(new ReadWork(selKey));
							selKey.interestOps(SelectionKey.OP_WRITE);
							selectedKeys.remove();
						}
						else if(selKey.isWritable())
						{
							if(pendingWrites.containsKey(selKey))
							{
								for(ByteBuffer b: pendingWrites.get(selKey))
								{
									tpm.addWork(new WriteWork(selKey, b));
								}
								pendingWrites.remove(selKey);
								selKey.interestOps(SelectionKey.OP_READ);
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
				System.out.println("Integer arguments expected");
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
