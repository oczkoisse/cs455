package cs455.overlay.wireformats;

import java.io.*;
import java.net.*;
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
	
	@Override
	public Iterator<LinkInfo> iterator() {
		
		return links.iterator();
	}
	
	public class LinkInfo
	{
		private String ipAddressA;
		private int portnumA;
		
		private String ipAddressB;
		private int portnumB;
		
		private int weight;
		
		public LinkInfo(InetSocketAddress addA, InetSocketAddress addB, int weight)
		{
			this.ipAddressA = addA.getHostString();
			this.portnumA = addA.getPort();
			
			this.ipAddressB = addB.getHostString();
			this.portnumB = addB.getPort();
			
			this.weight = weight;
		}
		
		public InetSocketAddress getAddressA()
		{
			return new InetSocketAddress(ipAddressA, portnumA);
		}
		
		public InetSocketAddress getAddressB()
		{
			return new InetSocketAddress(ipAddressB, portnumB);
		}
		
		public int getWeight()
		{
			return weight;
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

}
