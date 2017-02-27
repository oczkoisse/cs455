package cs455.scaling.works;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class WriteWork extends Work {
	
	private ByteBuffer data;
	
	public WriteWork(SelectionKey key, ByteBuffer data)
	{
		super(key);
		this.data = data.asReadOnlyBuffer();
	}

	@Override
	public WorkType getType() {
		return WorkType.WRITE;
	}

	public ByteBuffer getData() {
		return this.data;
	}
}