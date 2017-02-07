package cs455.overlay.transport;

import java.net.*;
import java.io.*;
import cs455.overlay.wireformats.*;

public abstract class TCPReceiverThread implements Runnable {
	
	protected Socket sock;
	protected DataInputStream din;
	
	public TCPReceiverThread(Socket s)
	{
		sock = s;
		try
		{
			din = new DataInputStream(sock.getInputStream());
		}
		catch(IOException e)
		{
			System.out.println("Unable to retrieve input stream from socket: " + e.getMessage());
			System.exit(0);
		}
		
	}
	
	public abstract void handleEvent(EventType evType);
	
	public void run()
	{
		try
		{
			int ordinalEvent;
			while(true)
			{
				ordinalEvent = din.readInt();
				EventType evType = EventType.valuesArr [ ordinalEvent ];
				handleEvent(evType);
			}
		}
		catch(EOFException e)
		{
			System.out.println("Remote socket closed: " + sock.getRemoteSocketAddress().toString() );
		}
		catch(IOException e)
		{
			System.out.println("Can't read event: " + e.getMessage());
			System.exit(0);
		}
		
		try
		{
			din.close();
			sock.close();
		}
		catch (IOException e)
		{
			System.out.println("Unable to close streams");
			System.exit(0);
		}
		
	}

}
