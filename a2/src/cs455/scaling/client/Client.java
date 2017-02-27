package cs455.scaling.client;

import java.io.IOException;
import java.net.*;
import java.util.*;


public class Client {
	
	private Payload payload;

	private String serverName;
	private int serverPort;	
	private int messageRate;
	
	private Socket serverCon;
	
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
			this.serverCon = new Socket(serverName, serverPort);
		}
		catch(IOException e)
		{
			System.out.println("Can't connect to server. Exiting...");
			System.exit(0);
		}
		
		this.sentCounter = 0;
		this.receivedCounter = 0;
		
		this.hashRecords = new LinkedList<String>();
	}
	
	public void start()
	{
		this.payload = new Payload(8, Payload.Unit.K_BYTES);
		
		while(true)
		{
			// Send the payload
			
			// Append the hash
			this.hashRecords.add(this.payload.getHash());
			
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub	
	}

}
