package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PullTrafficSummary implements Event {
	
	public PullTrafficSummary()
	{
		
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			dout.flush();
			
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}

	@Override
	public EventType getType() {

		return EventType.PULL_TRAFFIC_SUMMARY;
	}

}
