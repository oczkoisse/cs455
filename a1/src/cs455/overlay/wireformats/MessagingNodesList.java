package cs455.overlay.wireformats;

import java.io.*;
import java.util.*;
import java.net.*;

public class MessagingNodesList implements Event, Iterable<InetSocketAddress> {
	
	private Vector<InetSocketAddress> addresses;

	public MessagingNodesList()
	{
		addresses = new Vector<InetSocketAddress>(10);
	}
	
	public void add(String ipAddress, int port)
	{
		addresses.add(new InetSocketAddress(ipAddress, port));
	}
	
	public Iterator<InetSocketAddress> iterator()
	{
		return addresses.iterator();
	}
	
	public int size()
	{
		return addresses.size();
	}
	
	@Override
	public byte[] getBytes() throws IOException 
	{
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			dout.writeInt(this.size());
			
			for (InetSocketAddress a: addresses)
			{
				dout.writeUTF(a.getHostString());
				dout.writeInt(a.getPort());
			}

			dout.flush();
			
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}

	@Override
	public EventType getType() {
		
		return EventType.MESSAGING_NODES_LIST;
	}

}
