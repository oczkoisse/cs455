package cs455.scaling.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides convenience operations for computing SHA-1 hashes
 * Works with {@link java.nio.ByteBuffer}s to facilitate non-blocking I/O.
 * This class cannot be instantiated.
 * Class usage is thread-safe
 * @author Rahul Bangar
 *
 */
public class Hasher {
	
	/**
	 * The hash length for SHA-1 in bytes
	 */
	private static final int hashLength = 20;

	private Hasher()
	{		
	}
	
	/**
	 * Gets a new {@link java.security.MessageDigest} to compute SHA-1 hash
	 * @return
	 */
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
	
	/**
	 * Hashes the input {@link java.nio.ByteBuffer} to yield a new read-only {@link java.nio.ByteBuffer}.
	 * Assumes the input {@link java.nio.ByteBuffer} is consumable using {@link java.nio.ByteBuffer#get}
	 * The output {@link java.nio.ByteBuffer} is ready to be consumed using {@link java.nio.ByteBuffer#get()}
	 * @param data	the data to be hashed
	 * @return		SHA-1 hash of the input data
	 */
	public static ByteBuffer hash(ByteBuffer data)
	{
		MessageDigest msgDigest = getMessageDigest();
		byte[] toHash = new byte[data.remaining()];
		data.get(toHash);
		
		return ByteBuffer.wrap(msgDigest.digest(toHash)).asReadOnlyBuffer();
	}
	
	/**
	 * Same as {@link cs455.scaling.util.Hasher#hash(ByteBuffer)}, but returns
	 * a string instead containing the hexadecimal representation of the hash
	 * @param data	The data to be hashed
	 * @return		String containing hexadecimal representation of the hash
	 */
	public static String hashAsString(ByteBuffer data)
	{
		return convHashToString(hash(data));
	}
	
	/**
	 * Computes a hexadecimal string representation of hash
	 * Meant to be used along with @{link {@link cs455.scaling.util.Hasher#hash(ByteBuffer)}
	 * @param hash	the hash to be converted to a string form
	 * @return		a string representation of the input hash
	 */
	public static String convHashToString(ByteBuffer hash)
	{
		// Promise to not write to this buffer
		ByteBuffer temp = hash.asReadOnlyBuffer();
		
		byte[] hashArr = new byte[getHashLength()];
		temp.get(hashArr);
		
		BigInteger hashInt = new BigInteger(1, hashArr);
		return hashInt.toString(16);
	}
	
	/**
	 * Returns the length of SHA-1 hash in bytes i.e. 20
	 * @return	length of SHA-1 hash
	 */
	public static int getHashLength()
	{
		return hashLength;
	}
}
