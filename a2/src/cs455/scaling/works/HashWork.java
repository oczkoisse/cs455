package cs455.scaling.works;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class HashWork extends Work {

	private ByteBuffer data;
	
	public HashWork(SelectionKey key, ByteBuffer data)
	{
		super(key);
		this.data = data.asReadOnlyBuffer();
	}

	@Override
	public WorkType getType() {
		return WorkType.HASH;
	}

	public ByteBuffer getData() {
		return this.data;
	}
}
