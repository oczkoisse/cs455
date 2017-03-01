package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.*;

import cs455.scaling.util.Hasher;

import java.nio.ByteBuffer;
import java.nio.channels.*;

public class Client {
	
	private Payload payload;

	private String serverName;
	private int serverPort;	
	private int messageRate;
	
	private Selector selector;
	private SocketChannel selChannel;
	
	private int sentCounter;
	private int receivedCounter;
	
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
	
	public void run()
	{
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
								k.interestOps(SelectionKey.OP_WRITE);
							}
						}
						else if(k.isWritable())
						{
							ByteBuffer writeBuffer = this.payload.getData();
							
							while(writeBuffer.hasRemaining())
								this.selChannel.write(writeBuffer);
							
							// Append the hash
							this.hashRecords.add(this.payload.getHash());
							
							this.sentCounter++;
						}
						else if (k.isReadable())
						{
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
							
							this.receivedCounter++;
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
