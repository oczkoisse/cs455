package cs455.overlay.wireformats;

import java.io.*;

public class RegisterRequest implements Event 
{
	private String ipAddress;
	private int portnum;
	
	public RegisterRequest(String ip, int port)
	{
		ipAddress = ip;
		portnum = port;
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
			dout.writeUTF(ipAddress);
			dout.writeInt(portnum);
			dout.flush();
			
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}
}
