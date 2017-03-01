package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.*;

import cs455.scaling.util.Hasher;

import java.nio.ByteBuffer;
import java.nio.channels.*;

public class Client implements Runnable {
	
	private Payload payload;

	private String serverName;
	private int serverPort;	
	private int messageRate;
	
	private Selector selector;
	private SocketChannel selChannel;
	
	private Integer sentCounter;
	private Integer receivedCounter;
	
	private LinkedList<String> hashRecords;
	
	public Client(String serverName, int serverPort, int messageRate)
	{
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
			
		try
		{
			this.selector = Selector.open();
			this.selChannel = SocketChannel.open();
			this.selChannel.configureBlocking(false);
			this.selChannel.connect(new InetSocketAddress(this.serverName, this.serverPort));
			
			this.selChannel.register(this.selector, SelectionKey.OP_CONNECT);
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
	
	public void run()
	{
		Timer t = new Timer();
		t.scheduleAtFixedRate(new Summary(), new Date(), 10000);
		
		this.payload = new Payload(8, Payload.Unit.K_BYTES);
		
		while(true)
		{
			// Send the payload or receive the hash
			
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
					
					if(k.isValid())
					{
						if(k.isConnectable())
						{
							boolean success = ((SocketChannel) k.channel()).finishConnect();
							if(success)
							{
								System.out.println("Connected");
								k.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
							}
						}
						else
						{
							if(k.isWritable())
							{
								System.out.println("Writing");
								ByteBuffer writeBuffer = this.payload.getData();
								
								while(writeBuffer.hasRemaining())
									this.selChannel.write(writeBuffer);
								
								// Append the hash
								this.hashRecords.add(this.payload.getHash());
								
								synchronized(this.sentCounter)
								{
									this.sentCounter++;
								}
							}
							if (k.isReadable())
							{
								System.out.println("Reading");
								ByteBuffer readBuffer = ByteBuffer.allocate(Hasher.getHashLength());
								
								while(readBuffer.hasRemaining())
									this.selChannel.read(readBuffer);
								
								byte[] hash = new byte[Hasher.getHashLength()];
								BigInteger hashInt = new BigInteger(1, hash);
								String hashString = hashInt.toString(16);
								
								// Iterate on hashRecords and remove hashString
								Iterator<String> r = hashRecords.iterator();
								while(r.hasNext())
								{
									if(r.next().equals(hashString))
									{
										r.remove();
										break;
									}
								}
								
								synchronized(this.receivedCounter)
								{
									this.receivedCounter++;
								}
							}
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
			
			// Sleep for some time
			try
			{
				Thread.sleep(1000/this.messageRate);
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
