package cs455.scaling.works;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class WriteWork extends Work {
	
	private ByteBuffer data;
	
	public WriteWork(SelectionKey key, ByteBuffer data)
	{
		super(key, WorkType.WRITE);
		this.data = data.asReadOnlyBuffer();
	}

	public ByteBuffer getData() {
		return this.data;
	}
	
	public String toString()
	{
		return "Write work with data size " + data.remaining();
	}
}