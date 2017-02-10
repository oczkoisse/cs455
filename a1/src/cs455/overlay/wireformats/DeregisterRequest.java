package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeregisterRequest implements Event {

	private String msgNodeIpAddress;
	private int msgNodePort;
	
	public DeregisterRequest(String msgNodeIpAddress, int msgNodePort)
	{
		this.msgNodeIpAddress = msgNodeIpAddress;
		this.msgNodePort = msgNodePort;
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
			
			dout.writeUTF(msgNodeIpAddress);
			dout.writeInt(msgNodePort);
			
			dout.flush();
			
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}

	public String getIpAddress()
	{
		return msgNodeIpAddress;
	}
	
	public int getPort()
	{
		return msgNodePort;
	}

}
