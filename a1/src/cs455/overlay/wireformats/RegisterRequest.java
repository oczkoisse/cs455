package cs455.overlay.wireformats;

import java.io.*;

public class RegisterRequest implements Event 
{
	private final String hostName;
	private final int port;
	
	public RegisterRequest(String hostName, int port)
	{
		this.hostName = hostName;
		this.port = port;
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
			dout.writeUTF(hostName);
			dout.writeInt(port);
			dout.flush();
			
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}
	
	public String getHostName()
	{
		return hostName;
	}
	
	public int getPort()
	{
		return port;
	}
}
