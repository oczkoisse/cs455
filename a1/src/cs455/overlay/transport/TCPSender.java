package cs455.overlay.transport;

import java.net.*;
import java.io.*;

public class TCPSender {
	
	private Socket sock;
	private DataOutputStream dout;
	
	public TCPSender(Socket s) throws IOException
	{
		sock = s;
		dout = new DataOutputStream(sock.getOutputStream());
	}

	public void send(byte[] data) throws IOException
	{
		dout.write(data);
		dout.flush();
	}
	
	public Socket getSock()
	{
		return sock;
	}
}
