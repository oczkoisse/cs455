package cs455.overlay.wireformats;

import java.io.*;

public class MessagingNodesList implements Event {
	
	int count;
	String[] ipAddresses;
	int[] ports;

	public MessagingNodesList(int count, String[] ipAddresses, int[] ports)
	{
		if (count >= 0)
		{
			this.count = count;
			if (ipAddresses.length == ports.length)
			{
				this.ipAddresses = ipAddresses;
				this.ports = ports;
			}
			else
			{
				throw new IllegalArgumentException("Incompatible lengths of list of IP addresses and ports");
			}
		}
		else
		{
			throw  new IllegalArgumentException("Negative length passed for constructing messaging list"); 
		}
	}
	
	@Override
	public byte[] getBytes() throws IOException 
	{
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			dout.writeInt(this.count);
			
			for(int i=0; i<count; i++)
			{
				dout.writeUTF(ipAddresses[i]);
				dout.writeInt(ports[i]);
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
