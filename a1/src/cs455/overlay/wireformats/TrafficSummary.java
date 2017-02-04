package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficSummary implements Event {
	
	private String ipAddress;
	private int portnum;
	
	private int sentCount,
				sentSum,
				receivedCount,
				receivedSum,
				relayedCount;
	
	public TrafficSummary(String ipAddress, int portnum, int sentCount, int sentSum, int receivedCount, int receivedSum, int relayedCount)
	{
		this.ipAddress = ipAddress;
		this.portnum = portnum;
		
		this.sentCount = sentCount;
		this.sentSum = sentSum;
		
		this.receivedCount = receivedCount;
		this.receivedSum = receivedSum;
		
		this.relayedCount = relayedCount;
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
			
			dout.writeInt(sentCount);
			dout.writeInt(sentSum);
			dout.writeInt(receivedCount);
			dout.writeInt(receivedSum);
			dout.writeInt(relayedCount);
			
			dout.flush();
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}

	@Override
	public EventType getType() {
		return EventType.TRAFFIC_SUMMARY;
	}

}
