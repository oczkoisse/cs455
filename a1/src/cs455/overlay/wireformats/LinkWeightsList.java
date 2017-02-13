package cs455.overlay.wireformats;

import java.io.*;
import java.util.*;

public class LinkWeightsList implements Event, Iterable<LinkWeightsList.LinkInfo>
{

	private Vector<LinkInfo> links;
	
	@Override
	public byte[] getBytes() throws IOException 
	{
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
			{
				dout.writeInt(this.getType().ordinal());
				dout.writeInt(this.size());
				
				for(LinkInfo l: this.links)
				{
					dout.write(l.getBytes());
				}
				
				dout.flush();
				
				bytes = bout.toByteArray();
			}
			
		return bytes;
	}
	
	public int size()
	{
		return links.size();
	}

	@Override
	public EventType getType() 
	{	
		return EventType.LINK_WEIGHTS_LIST;
	}
	
	public LinkWeightsList()
	{
		this.links = new Vector<LinkInfo>(20);
	}
	
	public void add(LinkInfo linfo)
	{
		this.links.add(linfo);
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
		
		private byte[] getBytes() throws IOException
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

	@Override
	public Iterator<LinkInfo> iterator() {
		
		return links.iterator();
	}

}
