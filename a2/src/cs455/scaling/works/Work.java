package cs455.scaling.works;

import java.nio.channels.SelectionKey;

public abstract class Work {

	private final SelectionKey key;
	private final WorkType wtype;
	
	public Work(SelectionKey key, WorkType wtype)
	{
		this.key = key;
		this.wtype = wtype;
	}
	
	public final SelectionKey getSelectionKey()
	{
		return key;
	}

	public final WorkType getType()
	{
		return wtype;
	}
	
	public abstract String toString();

}

