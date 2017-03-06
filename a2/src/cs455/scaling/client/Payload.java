package cs455.scaling.client;

import java.util.*;
import java.nio.ByteBuffer;

import cs455.scaling.util.Hasher;

/**
 * Abstraction for the payload data.
 * Meant to be used by {@link cs455.scaling.client.Client}.
 * @author Rahul Bangar
 *
 */
public class Payload {
	
	/**
	 * Internal storage for the payload data
	 */
	private byte[] data;

	/**
	 * Random number generator to refresh the payload data
	 */
	private final Random rng;
	
	/**
	 * The hash of the data in the form of hexadecimal string
	 * Internally computed using {@link cs455.scaling.util.Hasher}
	 */
	private String hashString;
	
	
	/**
	 * Initializes the payload with random data of specified size
	 * @param size 	the size of the payload
	 * @param u 	the unit of specified size (bytes, kilobytes)
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
	
	/**
	 * Recomputes the hash of the payload data
	 */
	private void updateHashString()
	{
		this.hashString = Hasher.hashAsString(ByteBuffer.wrap(this.data));
	}
	
	/**
	 * Refreshes the internal payload data with new random data
	 */
	private void updateData()
	{
		rng.nextBytes(this.data);
	}
	
	/**
	 * Refreshes the data with new random data and recomputes the hash.
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
	 * Gets the read only form of input payload data
	 * @return Read-only form of input payload data
	 */
	public ByteBuffer getData()
	{
		return ByteBuffer.wrap(data).asReadOnlyBuffer();
	}
	
	/**
	 * Unit of payload data
	 * Can be bytes or kilo-bytes
	 * @author Rahul Bangar
	 *
	 */
	public enum Unit {
		BYTES,
		K_BYTES;
	}

}
