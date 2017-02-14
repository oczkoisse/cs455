package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom;

public class Message implements Event {
	
	private int payload;
	
	private String ipAddress;
	private int port;
	
	public Message(InetSocketAddress destination, int payload)
	{
		this.ipAddress = destination.getHostString();
		this.port = destination.getPort();
		this.payload = payload;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			dout.writeUTF(this.ipAddress);
			dout.writeInt(this.port);
			
			dout.writeInt(this.payload);
			dout.flush();
			
			bytes = bout.toByteArray();
		}
		
		return bytes;

	}
	
	public String getDestination()
	{
		return ipAddress + ":" + port;
	}
	
	public int getPayload()
	{
		return payload;
	}

	@Override
	public EventType getType() {
		
		return EventType.MESSAGE;
	}

}
