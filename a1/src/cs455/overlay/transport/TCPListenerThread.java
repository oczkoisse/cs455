package cs455.overlay.transport;

import java.net.*;
import java.io.*;

public abstract class TCPListenerThread implements Runnable {
	
	protected ServerSocket sock;
	
	public TCPListenerThread(int port)
	{
		try
		{
			sock = new ServerSocket(port);
		}
		catch(IOException e)
		{
			System.out.println("Unable to create server socket: " + e.getMessage());
		}
	}
	
	public TCPListenerThread()
	{
		this(0);
	}
	
	public abstract void handleClient(Socket s);
	
	public void close() throws IOException
	{
		sock.close();
	}
	
	public int getLocalPort()
	{
		return sock.getLocalPort();
	}
	
	public String getLocalIpAddress()
	{
		return sock.getInetAddress().getHostAddress();
	}
	
	public void run()
	{
		while (true)
		{
			try
			{
				Socket s = sock.accept();
				System.out.println("Accepted " + s.getInetAddress().getHostAddress() + ":" + s.getPort());
				handleClient(s);
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
			}
		}
	}

}
