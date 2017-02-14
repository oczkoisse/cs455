package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PeerConnect implements Event {

	String ipAddress;
	int port;
	
	public PeerConnect(String ipAddress, int port)
	{
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	@Override
	public byte[] getBytes() throws IOException {
byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			dout.writeUTF(ipAddress);
			dout.writeInt(port);
		
			dout.flush();
			
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}
	
	public String getIpAddress()
	{
		return ipAddress;
	}
	
	public int getPort()
	{
		return port;
	}

	@Override
	public EventType getType() {
		return EventType.PEER_CONNECT;
	}

}
