package cs455.scaling.client;

import java.util.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;

public class Payload {
	
	private byte[] data;

	private final Random rng;
	
	private String hashString;
	
	private MessageDigest hasher;
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
		
		try
		{
			hasher = MessageDigest.getInstance("SHA1");
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e.getMessage());
		}
		
		refresh();
		
	}
	
	private void updateHash()
	{
		hasher.reset();
		byte[] hash = hasher.digest(this.data);
		BigInteger hashInt = new BigInteger(1, hash);
		this.hashString = hashInt.toString(16);
	}
	
	/**
	 * Refreshes the data with new random data
	 * Also, recomputes the hash
	 */
	public void refresh()
	{
		rng.nextBytes(this.data);
		updateHash();
	}
	
	/**
	 * Returns a hash of the current payload as a string
	 * @return
	 */
	public String getHash()
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
