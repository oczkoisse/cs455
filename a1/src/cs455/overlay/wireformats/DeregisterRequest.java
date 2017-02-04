package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeregisterRequest implements Event {

	private String ipAddress;
	private int portnum;
	
	public DeregisterRequest(String ip, int port)
	{
		ipAddress = ip;
		portnum = port;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.DEREGISTER_REQUEST;
	}
	
	@Override
	public byte[] getBytes() throws IOException
	{
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			
			dout.writeUTF(ipAddress);
			dout.writeInt(portnum);
			
			dout.flush();
			
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}

}
