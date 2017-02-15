package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.net.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskComplete implements Event {

	String ipAddress;
	int portnum;
	
	public TaskComplete(String ipAddress, int portnum)
	{
		this.ipAddress = ipAddress;
		this.portnum = portnum;
	}
	
	@Override
	public byte[] getBytes() throws IOException {
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
	
	public InetSocketAddress getAddress()
	{
		return new InetSocketAddress(ipAddress, portnum);
	}

	@Override
	public EventType getType() {

		return EventType.TASK_COMPLETE;
	}

}
