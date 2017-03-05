package cs455.scaling.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Thread safe
public class Hasher {
	
	private static final int hashLength = 20; //SHA-1 is 20 bytes long

	private Hasher()
	{		
	}
	
	private static MessageDigest getMessageDigest()
	{
		MessageDigest msgDigest = null;
		try
		{
			msgDigest = MessageDigest.getInstance("SHA1");
		}
		catch(NoSuchAlgorithmException e)
		{
			System.out.println(e.getMessage());
		}
		return msgDigest;
	}
	
	public static ByteBuffer hash(ByteBuffer data)
	{
		MessageDigest msgDigest = getMessageDigest();
		byte[] toHash = new byte[data.remaining()];
		data.get(toHash);
		
		return ByteBuffer.wrap(msgDigest.digest(toHash)).asReadOnlyBuffer();
	}
	
	public static String hashAsString(ByteBuffer data)
	{
		return convHashToString(hash(data));
	}
	
	// Assume that client code will get 'hash' ready to be drained
	public static String convHashToString(ByteBuffer hash)
	{
		// Promise to not write to this buffer
		ByteBuffer temp = hash.asReadOnlyBuffer();
		
		byte[] hashArr = new byte[getHashLength()];
		temp.get(hashArr);
		
		BigInteger hashInt = new BigInteger(1, hashArr);
		return hashInt.toString(16);
	}
	
	public static int getHashLength()
	{
		return hashLength;
	}
}
