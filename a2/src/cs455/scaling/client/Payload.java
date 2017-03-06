package cs455.scaling.client;

import java.util.*;
import java.nio.ByteBuffer;

import cs455.scaling.util.Hasher;

// Not thread safe
public class Payload {
	
	private byte[] data;

	private final Random rng;
	
	private String hashString;
	
	
	/**
	 * Initializes the payload with random data of specified size
	 * @param size The size of the payload
	 * @param u The unit of specified size (bytes, kilobytes)
	 */
	public Payload(int size, Unit u)
	{
		int multiplier;
		switch(u)
		{
		case BYTES: 
			multiplier = 1;
			break;
		case K_BYTES: 
			multiplier = 1024;
			break;
		default:
			throw new IllegalArgumentException("Invalid size type passed to Payload");
		}
		
		data = new byte[size * multiplier];
		rng = new Random();
		
		refresh();
		
	}
	
	private void updateHashString()
	{
		this.hashString = Hasher.hashAsString(ByteBuffer.wrap(this.data));
	}
	
	private void updateData()
	{
		rng.nextBytes(this.data);
	}
	
	/**
	 * Refreshes the data with new random data
	 * Also, recomputes the hash
	 */
	public void refresh()
	{
		updateData();
		updateHashString();
	}
	
	/**
	 * Returns a hash of the current payload as a string
	 * @return
	 */
	public String getHashString()
	{
		return hashString;
	}
	
	/**
	 * Returns a read only form of payload data
	 * @return
	 */
	public ByteBuffer getData()
	{
		return ByteBuffer.wrap(data).asReadOnlyBuffer();
	}
	
	public enum Unit {
		BYTES,
		K_BYTES;
	}

}
