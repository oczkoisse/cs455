package cs455.overlay.wireformats;

import java.io.*;
import java.util.*;
import java.net.*;

public class MessagingNodesList implements Event {
	
	Vector<InetSocketAddress> addresses;

	public MessagingNodesList()
	{
		addresses = new Vector<InetSocketAddress>(10);
	}
	
	public void add(String ipAddress, int port)
	{
		addresses.add(new InetSocketAddress(ipAddress, port));
	}
	
	public Vector<InetSocketAddress> getAddresses()
	{
		return (Vector<InetSocketAddress>) addresses.clone();
	}
	
	@Override
	public byte[] getBytes() throws IOException 
	{
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			dout.writeInt(addresses.size());
			
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
