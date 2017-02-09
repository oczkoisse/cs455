package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterResponse implements Event {
	
	private byte status;
	
	private String info;

	public RegisterResponse(boolean status, String info)
	{
		this.status = status ? (byte) 1 : (byte) 0;
		this.info = info;
	}
	@Override
	public byte[] getBytes() throws IOException {
		
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			dout.writeByte(status);
			dout.writeUTF(info);

			dout.flush();
			
			bytes = bout.toByteArray();
		}
			
		return bytes;
			
	}
	
	public boolean getStatus()
	{
		return (status == (byte) 1) ? true : false;
	}
	
	public String getInfo()
	{
		return info;
	}

	@Override
	public EventType getType() {
		
		return EventType.REGISTER_RESPONSE;
	}

}
