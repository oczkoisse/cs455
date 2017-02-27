package cs455.scaling.client;

import java.util.*;
import java.math.BigInteger;
import java.security.*;

public class Payload {
	
	private byte[] data;

	private Random rng;
	
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
	
	private void updateData(byte[] data)
	{
		if (this.data.length == data.length)
		{
			System.arraycopy(data, 0, this.data, 0, this.data.length);
		}
		else
			throw new IllegalArgumentException("Input array is too large for payload");
	}
	
	private void updateHash()
	{
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
	 * Returns a hash of the current payload
	 * @return
	 */
	public String getHash()
	{
		return hashString;
	}
	
	/**
	 * Returns a copy of payload data
	 * @return
	 */
	public byte[] getBytes()
	{
		return data.clone();
	}
	
	public enum Unit {
		BYTES,
		K_BYTES;
	}

}
