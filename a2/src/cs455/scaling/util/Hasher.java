package cs455.scaling.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {
	private MessageDigest msgDigest;
	
	public Hasher()
	{
		try
		{
			msgDigest = MessageDigest.getInstance("SHA1");
		}
		catch(NoSuchAlgorithmException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	public ByteBuffer hash(ByteBuffer data)
	{
		ByteBuffer rdata = data.asReadOnlyBuffer();
		
		// Allocate 160 bits for SHA1 hash
		byte[] toHash = new byte[rdata.remaining()];
		rdata.get(toHash);
		
		return ByteBuffer.wrap(msgDigest.digest(toHash));
	}
	
	public static int getHashLength()
	{
		return 20;
	}
	
	/*
	 * Source: http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
	 * Only used to test the working
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	*/
	
	/*
	public static void main(String[] args)
	{
		String s = "The quick brown fox jumps over the lazy dog";
		Hasher h = new Hasher();
		ByteBuffer o = h.hash(ByteBuffer.wrap(s.getBytes()));
		
		System.out.println(bytesToHex(o.array()));
	}
	*/
}
