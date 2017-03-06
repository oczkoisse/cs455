package cs455.scaling.client;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import cs455.scaling.util.Hasher;


public class Client implements Runnable {
	
	/**
	 * The current payload to be sent.
	 */
	private Payload payload;

	/**
	 * The name of the host at which server resides
	 */
	private final String serverName;
	
	/**
	 * The port at which the server is listening for connections
	 */
	private final int serverPort;	
	
	/**
	 * The rate at which to send messages to the server
	 */
	private final int messageRate;
	
	/**
	 * One and only selector to multiplex network communications
	 */
	private Selector selector;
	
	/**
	 * The channel to communicate with the server
	 */
	private SocketChannel serverChannel;
	
	/**
	 * Counter for number of messages sent
	 */
	private Integer sentCounter;
	
	/**
	 * Counter for the number of messages acknowledged
	 */
	private Integer receivedCounter;
	
	/**
	 * List of hashs of the sent messages that have not been acknowledged
	 */
	private LinkedList<String> hashRecords;
	
	/**
	 * Instantiated the client instance by initiating the connection to the server
	 * @param serverName	host name of the server
	 * @param serverPort	the port at which the server is listening
	 * @param messageRate	the rate at which messages will be sent (in messages/second) to the server
	 */
	public Client(String serverName, int serverPort, int messageRate)
	{
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
			
		try
		{
			this.selector = Selector.open();
			this.serverChannel = SocketChannel.open();
			this.serverChannel.configureBlocking(false);
			this.serverChannel.connect(new InetSocketAddress(this.serverName, this.serverPort));
			
			this.serverChannel.register(this.selector, SelectionKey.OP_CONNECT);
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
		
		this.sentCounter = 0;
		this.receivedCounter = 0;
		
		this.hashRecords = new LinkedList<String>();
	}
	
	/**
	 * Prints the summary of server communications.
	 * Meant to be used along with {@link java.util.Timer} to schedule at a fixed rate
	 * @author Rahul Bangar
	 *
	 */
	private class Summary extends TimerTask
	{
		public void run()
		{
			synchronized(System.out)
			{
				synchronized(sentCounter)
				{
					synchronized(receivedCounter)
					{
						System.out.println("[" + super.scheduledExecutionTime() + "] " 		+
								   "Total Sent Count: "	 									+
									sentCounter + ", "							 			+
								   "Total Received Count: "		 							+
									receivedCounter);
						
						sentCounter = 0;
						receivedCounter = 0;
					}
				}
			}
			
		}
	}
	
	/**
	 * Begins the Client communication logic.
	 * Begins by scheduling the client summary to be printed every 10 seconds
	 * Sends the messages to the server at the initialize {@link cs455.scaling.client.Client#messageRate}
	 * Also, reads the acknowledgement in the form of hashes received from the server.
	 * Every message sent is hashed, and the resulting hash is stored for later verification.
	 */
	public void run()
	{
		// Timer for printing summary
		Timer t = new Timer();
		t.scheduleAtFixedRate(new Summary(), new Date(), 10000);

		this.payload = new Payload(8, Payload.Unit.K_BYTES);
		
		while(true)
		{
			// Send the payload or receive the hash
			long start = System.nanoTime();
			try
			{
				this.selector.select();
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
				System.exit(0);
			}
			
			Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
			
			try
			{
				while(selectedKeys.hasNext())
				{
					SelectionKey k = selectedKeys.next();
					
					if (!k.isValid())
						continue;
					
					if(k.isConnectable())
					{
						boolean success = ((SocketChannel) k.channel()).finishConnect();
						if(success)
						{
							// System.out.println("Connected");
							k.interestOps(SelectionKey.OP_WRITE);
						}
					}
					else
					{
						if (k.isReadable())
						{
							System.out.println("Reading");
							ByteBuffer readBuffer = ByteBuffer.allocate(Hasher.getHashLength());
							
							while(readBuffer.hasRemaining())
								this.serverChanel.read(readBuffer);
							
							readBuffer.flip();
							
							String hashString = Hasher.convHashToString(readBuffer);
							// Iterate on hashRecords and remove hashString
							Iterator<String> hashes = hashRecords.iterator();
							while(hashes.hasNext())
							{
								if(hashes.next().equals(hashString))
								{
									hashes.remove();
									
									synchronized(this.receivedCounter)
									{
										this.receivedCounter++;
									}
									
									break;
								}
							}
							
							k.interestOps(SelectionKey.OP_WRITE);
						}
						if(k.isWritable())
						{
							System.out.println("Writing");
							ByteBuffer writeBuffer = this.payload.getData();
							
							while(writeBuffer.hasRemaining())
								this.serverChannel.write(writeBuffer);
							
							// Append the hash
							this.hashRecords.add(this.payload.getHashString());
							
							synchronized(this.sentCounter)
							{
								this.sentCounter++;
							}
							k.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
						}
					}
					
					selectedKeys.remove();
				}
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
				System.exit(0);
			}
			
			long end = System.nanoTime();
			
			// Remaining time window in which the thread should sleep to preserve the message rate
			long timeToSleep = (1000/this.messageRate) - ((end-start) / 1000000);
			
			timeToSleep = timeToSleep > 0 ? timeToSleep : 0;
			// Sleep for some time according to messageRate
			// System.out.println("Sleeping for " + timeToSleep);
			try
			{
				Thread.sleep(timeToSleep);
			}
			catch(InterruptedException e)
			{
				System.out.println("Client interrupted");
				break;
			}
			
			// Refresh the payload
			this.payload.refresh();
		}
		
	}
	
	public static void main(String[] args)
	{
		if(args.length == 3)
		{
			String serverName = args[0];
			int serverPort, messageRate;
			try
			{
				serverPort = Integer.parseInt(args[1]);
				messageRate = Integer.parseInt(args[2]);
				Client c = new Client(serverName, serverPort, messageRate);
				c.run();
			}
			catch(NumberFormatException e)
			{
				System.out.println("Integer arguments expected");
				System.out.println("Usage: cs455.scaling.client.Client <server-name> <server-port> <message-rate>");
				System.exit(0);
			}
			
		}
		else
		{
			System.out.println("Usage: cs455.scaling.client.Client <server-name> <server-port> <message-rate>");
		}
	}

}
