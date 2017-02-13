package cs455.overlay.transport;

import java.net.*;
import java.io.*;
import cs455.overlay.wireformats.*;

public abstract class TCPReceiverThread implements Runnable {
	
	protected Socket sock;
	protected DataInputStream din;
	
	public TCPReceiverThread(Socket sock)
	{
		this.sock = sock;
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
	
	private EventType readEventType() throws IOException
	{
		int ordinalEvent = din.readInt();
		return EventType.valuesArr [ ordinalEvent ];
	}
	
	public void close() throws IOException
	{
		sock.close();
	}
	
	public void run()
	{
		//System.out.println("Handling " + this.sock.getInetAddress().getHostAddress() + ":" + this.sock.getPort());
		try
		{
			while (true)
			{
				EventType evType = readEventType();
				System.out.println("Received event " + evType.toString());
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
		}

		try
		{
			System.out.println("Closing the connection");
			din.close();
			sock.close();
		}
		catch (IOException e)
		{
			System.out.println("Unable to close the connection");
		}
	}

}
