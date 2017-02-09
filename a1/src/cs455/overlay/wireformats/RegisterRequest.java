package cs455.overlay.wireformats;

import java.io.*;

public class RegisterRequest implements Event 
{
	private final String msgNodeIpAddress;
	private final int msgNodePort;
	
	public RegisterRequest(String msgNodeIpAddress, int msgNodePort)
	{
		this.msgNodeIpAddress = msgNodeIpAddress;
		this.msgNodePort = msgNodePort;
	}
	
	
	@Override
	public EventType getType()
	{
		return EventType.REGISTER_REQUEST;
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
