package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LinkWeights implements Event 
{

	LinkWeights.LinkInfo[] links;
	
	@Override
	public byte[] getBytes() throws IOException 
	{
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
			{
				dout.writeInt(this.getType().ordinal());
				dout.writeInt(this.links.length);
				
				for(LinkInfo l: this.links)
				{
					dout.write(l.getBytes());
				}
				
				dout.flush();
				
				bytes = bout.toByteArray();
			}
			
		return bytes;
	}

	@Override
	public EventType getType() 
	{	
		return EventType.LINK_WEIGHTS;
	}
	
	public LinkWeights(LinkWeights.LinkInfo[] links)
	{
		this.links = links;
	}
	
	public class LinkInfo
	{
		private String ipAddressA;
		private int portnumA;
		
		private String ipAddressB;
		private int portnumB;
		
		private int weight;
		
		public LinkInfo(String ipAddressA, int portnumA, String ipAddressB, int portnumB, int weight)
		{
			this.ipAddressA = ipAddressA;
			this.portnumA = portnumA;
			
			this.ipAddressB = ipAddressB;
			this.portnumB = portnumB;
			
			this.weight = weight;
		}
		
		public byte[] getBytes() throws IOException
		{
			byte[] bytes = null;
			
			try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
					DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
			{
				dout.writeUTF(ipAddressA);
				dout.writeInt(portnumA);
				
				dout.writeUTF(ipAddressB);
				dout.writeInt(portnumB);
				
				dout.writeInt(weight);
					
				dout.flush();
				
				bytes = bout.toByteArray();
			}
				
			return bytes;
		}
		
	}

}
