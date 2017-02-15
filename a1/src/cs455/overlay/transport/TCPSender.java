package cs455.overlay.transport;

import java.net.*;
import java.io.*;

public class TCPSender {
	
	private Socket sock;
	private DataOutputStream dout;
	
	public TCPSender(Socket s)
	{
		try
		{
			sock = s;
			dout = new DataOutputStream(sock.getOutputStream());
		}
		catch(IOException e)
		{
			System.out.println("Can't open output stream from socket");
		}
	}

	public void send(byte[] data) throws IOException
	{
		dout.write(data);
		dout.flush();
		//System.out.println(sock.getLocalAddress().getHostAddress() + ":" + sock.getLocalPort() + " wrote " + data.length + " bytes to " + sock.getInetAddress().getHostAddress() + ":" + sock.getPort());
	}
	
	public Socket getSock()
	{
		return sock;
	}
}
