package cs455.scaling.works;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class HashWork extends Work {

	private ByteBuffer data;
	
	public HashWork(SelectionKey key, ByteBuffer data)
	{
		super(key, WorkType.HASH);
		this.data = data.asReadOnlyBuffer();
	}

	public ByteBuffer getData() {
		return this.data;
	}
	
	public String toString()
	{
		return "Hash work with data size " + data.remaining();
	}
}
