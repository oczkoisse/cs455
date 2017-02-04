package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterResponse implements Event {
	
	private boolean status;
	
	private String info;

	public RegisterResponse(boolean status, String info)
	{
		this.status = status;
		this.info = info;
	}
	@Override
	public byte[] getBytes() throws IOException {
		
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			dout.writeBoolean(status);
			dout.writeUTF(info);

			dout.flush();
			
			bytes = bout.toByteArray();
		}
			
		return bytes;
			
	}

	@Override
	public EventType getType() {
		
		return EventType.REGISTER_RESPONSE;
	}

}
