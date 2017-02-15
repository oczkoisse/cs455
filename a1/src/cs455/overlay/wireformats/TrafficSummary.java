package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.net.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficSummary implements Event {
	
	private String ipAddress;
	private int portnum;
	
	private int sentCount,
				receivedCount,
				relayedCount;
	
	private long receivedSum,
				 sentSum;
	
	public TrafficSummary(String ipAddress, int portnum, int sentCount, long sentSum, int receivedCount, long receivedSum, int relayedCount)
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
			dout.writeLong(sentSum);
			dout.writeInt(receivedCount);
			dout.writeLong(receivedSum);
			dout.writeInt(relayedCount);
			
			dout.flush();
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}
	
	public InetSocketAddress getAddress()
	{
		return new InetSocketAddress(ipAddress, portnum);
	}
	
	public int getSentCount()
	{
		return sentCount;
	}
	
	public int getReceivedCount()
	{
		return receivedCount;
	}
	
	public long getSentSummation()
	{
		return sentSum;
	}
	
	public long getReceivedSummation()
	{
		return receivedSum;
	}
	
	public int getRelayCount()
	{
		return relayedCount;
	}

	@Override
	public EventType getType() {
		return EventType.TRAFFIC_SUMMARY;
	}

}
